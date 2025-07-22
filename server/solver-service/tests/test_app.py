import unittest
import json
import numpy as np
from unittest.mock import patch, MagicMock
import sys
import os

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import (
    app,
    log_event,
    solve_graph_csp,
    get_vertices,
    find_edges,
    color_map,
    GraphColoringCSP,
)


class TestSolverApp(unittest.TestCase):

    def setUp(self):
        """Set up test client and test data"""
        self.app = app.test_client()
        self.app.testing = True

        # Sample test data
        self.valid_request_data = {
            "image": [255] * (800 * 600 * 4),  # White image
            "width": 800,
            "height": 600,
            "userId": "test-user-123",
        }

        self.simple_request_data = {
            "image": [255, 255, 255, 255] * 4,  # 2x2 white image
            "width": 2,
            "height": 2,
            "userId": "test-user-123",
        }

    def test_app_context_loads(self):
        """Test that the Flask app context loads successfully"""
        with app.app_context():
            self.assertIsNotNone(app)

    def test_return_home_endpoint(self):
        """Test the home endpoint returns correct response"""
        response = self.app.get("/")
        self.assertEqual(response.status_code, 200)

        data = json.loads(response.data)
        self.assertIn("message", data)
        self.assertIn("people", data)
        self.assertEqual(data["message"], "Hello World!!!")
        self.assertIsInstance(data["people"], list)
        self.assertIn("Sheikh", data["people"])

    def test_health_endpoint(self):
        """Test the health check endpoint"""
        response = self.app.get("/health")
        self.assertEqual(response.status_code, 200)

        data = json.loads(response.data)
        self.assertEqual(data["status"], "healthy")

    @patch("app.log_event")
    def test_solve_endpoint_with_valid_data(self, mock_log_event):
        """Test the solve endpoint with valid input data"""
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_request_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertIsInstance(data, list)

        # Verify logging was called
        mock_log_event.assert_called_once()

    def test_solve_endpoint_missing_json(self):
        """Test solve endpoint with no JSON data"""
        response = self.app.post("/api/solve")
        self.assertEqual(response.status_code, 400)

        data = json.loads(response.data)
        self.assertIn("error", data)
        self.assertEqual(data["error"], "No JSON data received")

    def test_solve_endpoint_missing_required_fields(self):
        """Test solve endpoint with missing required fields"""
        incomplete_data = {"width": 800, "height": 600}

        response = self.app.post(
            "/api/solve",
            data=json.dumps(incomplete_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertIn("error", data)
        self.assertEqual(data["error"], "Missing required fields")

    def test_solve_endpoint_invalid_pixel_data(self):
        """Test solve endpoint with invalid pixel data"""
        invalid_data = self.simple_request_data.copy()
        invalid_data["image"] = ["invalid", "pixel", "data"]

        response = self.app.post(
            "/api/solve", data=json.dumps(invalid_data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertIn("error", data)
        self.assertIn("Invalid pixel data", data["error"])

    def test_solve_endpoint_string_pixel_data(self):
        """Test solve endpoint with string pixel data that can be converted to int"""
        string_data = self.simple_request_data.copy()
        string_data["image"] = ["255", "255", "255", "255"] * 4

        response = self.app.post(
            "/api/solve", data=json.dumps(string_data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)

    def test_solve_endpoint_invalid_dimensions(self):
        """Test solve endpoint with invalid dimensions"""
        invalid_data = self.simple_request_data.copy()
        invalid_data["width"] = "invalid"

        response = self.app.post(
            "/api/solve", data=json.dumps(invalid_data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 500)

    def test_solve_endpoint_empty_user_id(self):
        """Test solve endpoint with empty user ID"""
        data = self.simple_request_data.copy()
        data["userId"] = ""

        response = self.app.post(
            "/api/solve", data=json.dumps(data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)

    def test_solve_endpoint_missing_user_id(self):
        """Test solve endpoint with missing user ID"""
        data = self.simple_request_data.copy()
        del data["userId"]

        response = self.app.post(
            "/api/solve", data=json.dumps(data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)

    @patch("app.solve_graph_csp")
    @patch("app.get_vertices")
    @patch("app.find_edges")
    @patch("app.color_map")
    def test_solve_endpoint_algorithm_flow(
        self, mock_color_map, mock_find_edges, mock_get_vertices, mock_solve_graph_csp
    ):
        """Test that the solve endpoint calls all algorithm functions in correct order"""
        # Set up mocks
        mock_vertices = [np.array([[1, 0], [0, 1]])]
        mock_black = np.array([[0, 1], [1, 0]])
        mock_vertice_matrix = np.array([[1, 0], [0, 1]])
        mock_get_vertices.return_value = (
            mock_vertices,
            mock_black,
            mock_vertice_matrix,
        )

        mock_edges = [(0, 1)]
        mock_find_edges.return_value = mock_edges

        mock_solution = {"0": "red", "1": "blue"}
        mock_solve_graph_csp.return_value = mock_solution

        mock_colored_map = np.array(
            [[[255, 0, 0], [0, 0, 255]], [[0, 0, 255], [255, 0, 0]]]
        )
        mock_color_map.return_value = mock_colored_map

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_request_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)

        # Verify all functions were called
        mock_get_vertices.assert_called_once()
        mock_find_edges.assert_called_once()
        mock_solve_graph_csp.assert_called_once()
        mock_color_map.assert_called_once()

    def test_solve_endpoint_large_image(self):
        """Test solve endpoint with larger image dimensions"""
        large_data = {
            "image": [0] * (100 * 100 * 4),  # Black 100x100 image
            "width": 100,
            "height": 100,
            "userId": "test-user-large",
        }

        response = self.app.post(
            "/api/solve", data=json.dumps(large_data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)

    def test_solve_endpoint_zero_dimensions(self):
        """Test solve endpoint with zero dimensions"""
        zero_data = {"image": [], "width": 0, "height": 0, "userId": "test-user-zero"}

        response = self.app.post(
            "/api/solve", data=json.dumps(zero_data), content_type="application/json"
        )

        self.assertEqual(response.status_code, 500)

    def test_solve_endpoint_negative_dimensions(self):
        """Test solve endpoint with negative dimensions"""
        negative_data = {
            "image": [255] * 16,
            "width": -2,
            "height": -2,
            "userId": "test-user-negative",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(negative_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 500)

    def test_solve_endpoint_mismatched_image_size(self):
        """Test solve endpoint with image data that doesn't match dimensions"""
        mismatched_data = {
            "image": [255] * 16,  # Data for 2x2 image
            "width": 4,  # But claiming 4x4
            "height": 4,
            "userId": "test-user-mismatch",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(mismatched_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 500)

    @patch("app.grpc.insecure_channel")
    def test_log_event_success(self, mock_channel):
        """Test successful logging event"""
        mock_stub = MagicMock()
        mock_response = MagicMock()
        mock_response.success = True
        mock_stub.LogEvent.return_value = mock_response
        mock_channel.return_value.__enter__.return_value = mock_channel.return_value
        mock_channel.return_value = MagicMock()

        # This should not raise an exception
        log_event("test-user", "test-event", "Test description")

    @patch("app.grpc.insecure_channel")
    def test_log_event_failure(self, mock_channel):
        """Test logging event with gRPC failure"""
        mock_stub = MagicMock()
        mock_response = MagicMock()
        mock_response.success = False
        mock_response.message = "Logging failed"
        mock_stub.LogEvent.return_value = mock_response
        mock_channel.return_value.__enter__.return_value = mock_channel.return_value
        mock_channel.return_value = MagicMock()

        # This should not raise an exception even on failure
        log_event("test-user", "test-event", "Test description")

    @patch("app.grpc.insecure_channel")
    def test_log_event_exception(self, mock_channel):
        """Test logging event with exception"""
        mock_channel.side_effect = Exception("Connection failed")

        # This should not raise an exception even when gRPC fails
        log_event("test-user", "test-event", "Test description")

    def test_solve_endpoint_concurrent_requests(self):
        """Test that multiple concurrent requests don't interfere"""
        import threading

        results = []
        errors = []

        def make_request():
            try:
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.simple_request_data),
                    content_type="application/json",
                )
                results.append(response.status_code)
            except Exception as e:
                errors.append(str(e))

        threads = []
        for _ in range(5):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()

        for thread in threads:
            thread.join()

        # All requests should succeed
        self.assertEqual(len(errors), 0)
        self.assertEqual(len(results), 5)
        for status_code in results:
            self.assertEqual(status_code, 200)


if __name__ == "__main__":
    unittest.main()
