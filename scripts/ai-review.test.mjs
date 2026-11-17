import test from 'node:test';
import assert from 'node:assert/strict';
import { MARKER, ROLES, roleMarker, reviewPlan } from './ai-review.mjs';

const env = { GITHUB_TOKEN: 'github-test', OPENAI_API_KEY: 'openai-test', PR_NUMBER: '12', REPOSITORY: 'owner/repo' };
function fixture(options = {}) {
  const calls = [];
  let reads = 0;
  const pr = { state: options.closed ? 'closed' : 'open', title: '계획',
    head: { sha: 'abc123', repo: { full_name: options.fork ? 'other/repo' : env.REPOSITORY } } };
  const fetchMock = async (url, request) => {
    const body = request.body && JSON.parse(request.body);
    calls.push({ url, ...request, body });
    let data;
    if (url.endsWith('/pulls/12')) {
      reads++;
      data = structuredClone(pr);
      if (options.changed && reads > 1) data.head.sha = 'newsha';
    } else if (url.includes('/pulls/12/files?')) {
      data = options.noPlans ? [] : [{ filename: 'docs/plan/issue-1.md', status: 'added' }];
    } else if (url.includes('/contents/')) {
      assert.ok(url.endsWith('?ref=abc123'));
      data = { type: 'file', encoding: 'base64', content: Buffer.from('계획 내용').toString('base64') };
    } else if (url === 'https://api.openai.com/v1/responses') {
      assert.equal(request.headers.Authorization, 'Bearer openai-test');
      assert.ok(body.input.includes('계획 내용'));
      assert.equal(body.tools, undefined);
      if (options.apiError) return { ok: false, status: 429 };
      data = { status: options.incomplete ? 'incomplete' : 'completed',
        output: [{ type: 'message', content: [{ type: 'output_text', text: options.empty ? '' : '테스트 계획을 보완하세요.' }] }] };
    } else if (url.includes('/comments?')) {
      if (options.paginated && url.endsWith('page=1')) {
        data = Array.from({ length: 100 }, (_, id) => ({ id, body: 'unrelated', user: { login: 'human' } }));
      } else {
        data = [{ id: 8, body: MARKER, user: { login: 'human' } }];
        if (options.existing) data.push(...ROLES.map((role, index) => ({
          id: 99 + index, body: `[${role.label}]\n${roleMarker(role)}`, user: { login: 'github-actions[bot]' }
        })));
        if (options.legacy) data.push({ id: 98, body: MARKER, user: { login: 'github-actions[bot]' } });
      }
    } else if (['POST', 'PATCH'].includes(request.method) && url.includes('/comments')) {
      assert.ok(ROLES.some(role => body.body.startsWith(`[${role.label}]\n${roleMarker(role)}`)));
      data = { id: 99 };
    } else throw new Error(`Unexpected request: ${url}`);
    return { ok: true, json: async () => data };
  };
  return { calls, fetchMock };
}

test('creates a review without overwriting a human marker comment', async () => {
  const f = fixture();
  assert.equal(await reviewPlan(env, f.fetchMock), 'published 3 role review comments');
  assert.equal(f.calls.at(-1).method, 'POST');
  assert.ok(f.calls.at(-1).url.endsWith('/issues/12/comments'));
  const posted = f.calls.filter(c => c.url.endsWith('/issues/12/comments'));
  assert.deepEqual(posted.map(c => c.body.body.split('\n')[0]), ['[개발 팀장]', '[기획 팀장]', '[운영 팀장]']);
  const prompts = f.calls.filter(c => c.url.includes('api.openai.com')).map(c => c.body.instructions);
  assert.equal(prompts.length, 3);
  ROLES.forEach((role, index) => assert.ok(prompts[index].includes(role.criteria)));
});

test('finds an existing bot comment on a later page and updates it', async () => {
  const f = fixture({ existing: true, paginated: true });
  assert.equal(await reviewPlan(env, f.fetchMock), 'published 3 role review comments');
  assert.equal(f.calls.at(-1).method, 'PATCH');
  assert.ok(f.calls.at(-1).url.endsWith('/issues/comments/101'));
  assert.equal(f.calls.filter(c => c.method === 'PATCH').length, 3);
});

for (const option of ['closed', 'fork', 'noPlans']) {
  test(`skips ${option} without an AI request or comment`, async () => {
    const f = fixture({ [option]: true });
    assert.match(await reviewPlan(env, f.fetchMock), /^skipped:/);
    assert.ok(f.calls.every(c => c.method === 'GET'));
  });
}

test('does not post stale results after a new commit', async () => {
  const f = fixture({ changed: true });
  assert.match(await reviewPlan(env, f.fetchMock), /changed during review/);
  assert.ok(!f.calls.some(c => c.url.includes('/comments')));
});

for (const option of ['incomplete', 'empty', 'apiError']) {
  test(`fails on ${option} without publishing a comment`, async () => {
    const f = fixture({ [option]: true });
    await assert.rejects(reviewPlan(env, f.fetchMock));
    assert.ok(!f.calls.some(c => c.url.includes('/comments')));
  });
}

test('rejects malformed PR numbers before any request', async () => {
  const f = fixture();
  await assert.rejects(reviewPlan({ ...env, PR_NUMBER: '12\nnumber=99' }, f.fetchMock), /Invalid PR/);
  assert.equal(f.calls.length, 0);
});

 test('migrates the old generic bot comment to the development role', async () => {
  const f = fixture({ legacy: true });
  await reviewPlan(env, f.fetchMock);
  const writes = f.calls.filter(c => c.url.includes('/comments') && ['POST', 'PATCH'].includes(c.method));
  assert.deepEqual(writes.map(c => c.method), ['PATCH', 'POST', 'POST']);
  assert.ok(writes[0].url.endsWith('/issues/comments/98'));
  assert.ok(writes[0].body.body.startsWith('[개발 팀장]'));
});
