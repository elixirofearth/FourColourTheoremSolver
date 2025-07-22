import unittest
import json
import numpy as np
import sys
import os
from unittest.mock import patch, MagicMock
import threading
import time

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import app


class TestSolverIntegration(unittest.TestCase):
    """Integration tests for the solver service"""

    @classmethod
    def setUpClass(cls):
        """Set up test client"""
        cls.app = app.test_client()
        cls.app.testing = True

    def setUp(self):
        """Set up test data for each test"""
        # Simple 4x4 image with clear regions (RGBA format - 4 values per pixel)
        self.simple_map_data = {
            "image": [
                # Top-left region (white) - 4 pixels
                255,
                255,
                255,
                255,  # pixel 1
                255,
                255,
                255,
                255,  # pixel 2
                255,
                255,
                255,
                255,  # pixel 3
                255,
                255,
                255,
                255,  # pixel 4
                # Top-right region (black boundary) - 4 pixels
                0,
                0,
                0,
                255,  # pixel 5
                0,
                0,
                0,
                255,  # pixel 6
                0,
                0,
                0,
                255,  # pixel 7
                0,
                0,
                0,
                255,  # pixel 8
                # Bottom-left region (black boundary) - 4 pixels
                0,
                0,
                0,
                255,  # pixel 9
                0,
                0,
                0,
                255,  # pixel 10
                0,
                0,
                0,
                255,  # pixel 11
                0,
                0,
                0,
                255,  # pixel 12
                # Bottom-right region (white) - 4 pixels
                255,
                255,
                255,
                255,  # pixel 13
                255,
                255,
                255,
                255,  # pixel 14
                255,
                255,
                255,
                255,  # pixel 15
                255,
                255,
                255,
                255,  # pixel 16
            ],
            "width": 4,
            "height": 4,
            "userId": "integration-test-user",
        }

        # Realistic map-like data
        self.realistic_map_data = {
            "image": self._create_realistic_map_image(),
            "width": 10,
            "height": 10,
            "userId": "realistic-test-user",
        }

        # Large map for performance testing
        self.large_map_data = {
            "image": self._create_large_map_image(),
            "width": 50,
            "height": 50,
            "userId": "large-test-user",
        }

    def _create_realistic_map_image(self):
        """Create a realistic map-like image with multiple regions"""
        # 10x10 image with 4 distinct regions
        image = []
        for y in range(10):
            for x in range(10):
                if x < 5 and y < 5:
                    # Top-left region
                    pixel = 255 if (x + y) % 3 != 0 else 0
                elif x >= 5 and y < 5:
                    # Top-right region
                    pixel = 255 if (x - y) % 3 != 0 else 0
                elif x < 5 and y >= 5:
                    # Bottom-left region
                    pixel = 255 if x % 2 == 0 else 0
                else:
                    # Bottom-right region
                    pixel = 255 if y % 2 == 0 else 0

                # RGBA format
                image.extend([pixel, pixel, pixel, 255])

        return image

    def _create_large_map_image(self):
        """Create a large map image for performance testing"""
        image = []
        for y in range(50):
            for x in range(50):
                # Create regions based on position
                region_x = x // 10
                region_y = y // 10
                region_id = region_x + region_y * 5

                # Make regions with some borders
                if x % 10 == 0 or y % 10 == 0:
                    pixel = 0  # Border
                else:
                    pixel = 255 if region_id % 2 == 0 else 128

                image.extend([pixel, pixel, pixel, 255])

        return image

    def test_full_pipeline_simple_map(self):
        """Test complete pipeline with simple map"""
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Should return a colored map
        self.assertIsInstance(data, list)
        self.assertEqual(len(data), 4)  # Height
        self.assertEqual(len(data[0]), 4)  # Width
        self.assertEqual(len(data[0][0]), 3)  # RGB

    def test_full_pipeline_realistic_map(self):
        """Test complete pipeline with realistic map"""
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.realistic_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Verify output structure
        self.assertIsInstance(data, list)
        self.assertEqual(len(data), 10)  # Height
        self.assertEqual(len(data[0]), 10)  # Width
        self.assertEqual(len(data[0][0]), 3)  # RGB

        # Verify colors are valid RGB values
        for row in data:
            for pixel in row:
                for channel in pixel:
                    self.assertIsInstance(channel, (int, float))
                    self.assertGreaterEqual(channel, 0)
                    self.assertLessEqual(channel, 255)

    def test_four_color_theorem_compliance(self):
        """Test that the solution uses at most 4 colors"""
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.realistic_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Extract unique colors
        unique_colors = set()
        for row in data:
            for pixel in row:
                # Convert to tuple for hashing
                color = tuple(pixel)
                if color != (0, 0, 0):  # Ignore black (borders)
                    unique_colors.add(color)

        # Should use at most 4 colors (excluding black borders)
        self.assertLessEqual(len(unique_colors), 4)

    def test_adjacent_regions_different_colors(self):
        """Test that adjacent regions have different colors"""
        # Create a simple 3x3 map with clear adjacency
        simple_adjacent_data = {
            "image": [
                # Row 1: region1, border, region2
                255,
                255,
                255,
                255,
                0,
                0,
                0,
                255,
                255,
                255,
                255,
                255,
                # Row 2: region1, border, region2
                255,
                255,
                255,
                255,
                0,
                0,
                0,
                255,
                255,
                255,
                255,
                255,
                # Row 3: border, border, border
                0,
                0,
                0,
                255,
                0,
                0,
                0,
                255,
                0,
                0,
                0,
                255,
                # Row 4: region3, border, region4
                255,
                255,
                255,
                255,
                0,
                0,
                0,
                255,
                255,
                255,
                255,
                255,
                # Row 5: region3, border, region4
                255,
                255,
                255,
                255,
                0,
                0,
                0,
                255,
                255,
                255,
                255,
                255,
            ],
            "width": 3,
            "height": 5,
            "userId": "adjacency-test-user",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(simple_adjacent_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Extract colors of regions (avoiding borders)
        region_colors = {}
        # Top-left region
        region_colors["top_left"] = tuple(data[0][0])
        # Top-right region
        region_colors["top_right"] = tuple(data[0][2])
        # Bottom-left region
        region_colors["bottom_left"] = tuple(data[3][0])
        # Bottom-right region
        region_colors["bottom_right"] = tuple(data[3][2])

        # Adjacent regions should have different colors
        self.assertNotEqual(region_colors["top_left"], region_colors["top_right"])
        self.assertNotEqual(region_colors["top_left"], region_colors["bottom_left"])
        self.assertNotEqual(region_colors["top_right"], region_colors["bottom_right"])
        self.assertNotEqual(region_colors["bottom_left"], region_colors["bottom_right"])

    @patch("app.log_event")
    def test_logging_integration(self, mock_log_event):
        """Test that logging is properly integrated"""
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)

        # Verify logging was called with correct parameters
        mock_log_event.assert_called_once()
        call_args = mock_log_event.call_args[0]

        self.assertEqual(call_args[0], "integration-test-user")  # user_id
        self.assertEqual(call_args[1], "map_coloring_completed")  # event_type
        self.assertEqual(call_args[2], "Successfully colored map")  # description

    def test_performance_large_map(self):
        """Test performance with large map"""
        start_time = time.time()

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.large_map_data),
            content_type="application/json",
        )

        end_time = time.time()
        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)

        # Should complete within reasonable time (30 seconds for large map)
        self.assertLess(processing_time, 30.0)

        # Verify output structure for large map
        data = json.loads(response.data)
        self.assertEqual(len(data), 50)  # Height
        self.assertEqual(len(data[0]), 50)  # Width

    def test_concurrent_requests(self):
        """Test handling of concurrent requests"""
        results = []
        errors = []

        def make_request():
            try:
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.simple_map_data),
                    content_type="application/json",
                )
                results.append(response.status_code)
            except Exception as e:
                errors.append(str(e))

        # Create multiple threads
        threads = []
        for i in range(5):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()

        # Wait for all threads to complete
        for thread in threads:
            thread.join()

        # All requests should succeed
        self.assertEqual(len(errors), 0)
        self.assertEqual(len(results), 5)
        for status_code in results:
            self.assertEqual(status_code, 200)

    def test_memory_usage_stability(self):
        """Test that memory usage remains stable across multiple requests"""
        import psutil
        import os

        process = psutil.Process(os.getpid())
        initial_memory = process.memory_info().rss

        # Make multiple requests
        for i in range(10):
            response = self.app.post(
                "/api/solve",
                data=json.dumps(self.simple_map_data),
                content_type="application/json",
            )
            self.assertEqual(response.status_code, 200)

        final_memory = process.memory_info().rss
        memory_increase = final_memory - initial_memory

        # Memory increase should be reasonable (less than 100MB)
        self.assertLess(memory_increase, 100 * 1024 * 1024)

    def test_error_handling_in_pipeline(self):
        """Test error handling throughout the pipeline"""
        # Test with corrupted image data
        corrupted_data = self.simple_map_data.copy()
        corrupted_data["image"] = [None, "invalid", {}, []]

        response = self.app.post(
            "/api/solve",
            data=json.dumps(corrupted_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertIn("error", data)

    def test_edge_cases_single_region(self):
        """Test edge case with single region"""
        single_region_data = {
            "image": [255, 255, 255, 255] * 4,  # All white 2x2 image
            "width": 2,
            "height": 2,
            "userId": "single-region-user",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(single_region_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Should return colored image
        self.assertEqual(len(data), 2)
        self.assertEqual(len(data[0]), 2)

    def test_edge_cases_all_black(self):
        """Test edge case with all black image"""
        all_black_data = {
            "image": [0, 0, 0, 255] * 4,  # All black 2x2 image
            "width": 2,
            "height": 2,
            "userId": "all-black-user",
        }

        response = self.app.post(
            "/api/solve",
            data=json.dumps(all_black_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Should return image (likely all black or minimal coloring)
        self.assertEqual(len(data), 2)
        self.assertEqual(len(data[0]), 2)

    def test_different_image_sizes(self):
        """Test with different image dimensions"""
        sizes = [(1, 1), (3, 3), (5, 7), (8, 6)]

        for width, height in sizes:
            with self.subTest(width=width, height=height):
                # Create test data for this size
                image_data = []
                for y in range(height):
                    for x in range(width):
                        # Create a simple pattern
                        pixel = 255 if (x + y) % 2 == 0 else 0
                        image_data.extend([pixel, pixel, pixel, 255])

                test_data = {
                    "image": image_data,
                    "width": width,
                    "height": height,
                    "userId": f"size-test-{width}x{height}",
                }

                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(test_data),
                    content_type="application/json",
                )

                self.assertEqual(response.status_code, 200)
                data = json.loads(response.data)
                self.assertEqual(len(data), height)
                self.assertEqual(len(data[0]), width)

    def test_color_consistency_across_requests(self):
        """Test that similar maps get consistent coloring"""
        # Make the same request twice
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)

        # Make the same request again
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.simple_map_data),
            content_type="application/json",
        )

        self.assertEqual(response.status_code, 200)
        data2 = json.loads(response.data)

        # Compare the two responses
        self.assertEqual(data, data2)


if __name__ == "__main__":
    unittest.main()
