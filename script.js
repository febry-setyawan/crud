
// This is a k6 load testing script that simulates a user journey.
// It logs in, manages users and roles, and verifies the API responses.

import http from 'k6/http';
import { sleep, check } from 'k6';

// k6 options for the load test.
export const options = {
  // Number of virtual users to simulate.
  vus: 20,
  // Duration of the test.
  duration: '1m',
};

// The base URL for the API.
const BASE_URL = 'http://localhost:8080/api';
// The login endpoint.
const LOGIN_URL = `${BASE_URL}/auth/login`;

// Test user credentials. Replace with a valid user from your database.
const TEST_USER = {
  username: 'admin@email.com',
  password: 's3cr3t',
};

// Generates a random string of a given length using only lowercase letters.
function randomString(length) {
  let chars = 'abcdefghijklmnopqrstuvwxyz';
  let str = '';
  for (let i = 0; i < length; i++) {
    str += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return str;
}

export default function () {
  // 1. Login to get a JWT token.
  let loginRes = http.post(LOGIN_URL, JSON.stringify(TEST_USER), {
    headers: { 'Content-Type': 'application/json' },
  });
  console.log('LOGIN', loginRes.status, loginRes.body, 'payload:', JSON.stringify(TEST_USER));
  check(loginRes, { 'login status 200': (r) => r.status === 200 });
  let accessToken = loginRes.json('accessToken') || loginRes.json('data.accessToken');
  if (!accessToken) {
    // Jika login gagal, skip iterasi
    return;
  }
  let authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${accessToken}`,
  };


  // 2. Get all roles to obtain a valid roleId.
  let resRole = http.get(`${BASE_URL}/roles`, { headers: authHeaders });
  console.log('GET /roles', resRole.status, resRole.body);
  check(resRole, { 'GET /roles status 200': (r) => r.status === 200 });
  let roles = resRole.json('content') || resRole.json();
  let roleId = null;
  if (Array.isArray(roles) && roles.length > 0) {
    // Ambil random roleId dari hasil GET roles
    roleId = roles[Math.floor(Math.random() * roles.length)].id;
  }

  // 3. Get all users.
  let res = http.get(`${BASE_URL}/users`, { headers: authHeaders });
  console.log('GET /users', res.status, res.body);
  check(res, { 'GET /users status 200': (r) => r.status === 200 });

  // 4. Create a new user (only if a valid roleId was found).
  if (roleId) {
    let now = Date.now();
    let username = `user_${randomString(5)}_${now}@test.com`;
    let payload = JSON.stringify({
      username: username,
      password: 'password123',
      roleId: roleId
    });
    let postRes = http.post(`${BASE_URL}/users`, payload, { headers: authHeaders });
    console.log('POST /users', postRes.status, postRes.body, 'payload:', payload);
    check(postRes, {
      'POST /users status 201/200': (r) => r.status === 201 || r.status === 200,
    });

    // 5. Get the user by ID (if created successfully).
    if (postRes.status === 201 || postRes.status === 200) {
      let userId = postRes.json('id') || postRes.json('data.id');
      if (userId) {
  let getById = http.get(`${BASE_URL}/users/${userId}`, { headers: authHeaders });
  console.log(`GET /users/${userId}`, getById.status, getById.body);
  check(getById, { 'GET /users/{id} status 200': (r) => r.status === 200 });
      }
    }
  }

  // 6. Create a new role.
  let now = Date.now();
  let roleName = `role_${randomString(4)}_${now}`;
  let rolePayload = JSON.stringify({
    name: roleName,
    description: 'Performance test role'
  });
  let postRole = http.post(`${BASE_URL}/roles`, rolePayload, { headers: authHeaders });
  console.log('POST /roles', postRole.status, postRole.body, 'payload:', rolePayload);
  check(postRole, {
    'POST /roles status 201/200': (r) => r.status === 201 || r.status === 200,
  });

  // 7. Get the role by ID (if created successfully).
  if (postRole.status === 201 || postRole.status === 200) {
    let roleId = postRole.json('id') || postRole.json('data.id');
    if (roleId) {
      let getRoleById = http.get(`${BASE_URL}/roles/${roleId}`, { headers: authHeaders });
      console.log(`GET /roles/${roleId}`, getRoleById.status, getRoleById.body);
      check(getRoleById, { 'GET /roles/{id} status 200': (r) => r.status === 200 });
    }
  }

  sleep(1);
}
