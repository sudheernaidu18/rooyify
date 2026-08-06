import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 100 }, // ramp up to 100 VUs
    { duration: '40s', target: 100 }, // stay at 100 VUs for 40s
    { duration: '10s', target: 0 },   // ramp down to 0 VUs
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // error rate should be less than 1%
    http_req_duration: ['p(95)<1000'], // 95% of requests should respond within 1000ms
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export default function () {
  // Test login endpoint (functional probe)
  const url = `${BASE_URL}/login.php`;
  const payload = JSON.stringify({
    email: 'test_load_user@example.com',
    password: 'password123',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);
  
  check(res, {
    'status is 200 or 401': (r) => r.status === 200 || r.status === 401,
    'transaction time OK': (r) => r.timings.duration < 1500,
  });

  sleep(0.5);
}
