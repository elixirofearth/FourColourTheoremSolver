import unittest
import time
import json
import sys
import os
import threading
import numpy as np
from unittest.mock import patch
import psutil

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import app, solve_graph_csp, get_vertices, find_edges, GraphColoringCSP


class TestSolverPerformance(unittest.TestCase):
    """Performance tests for the solver service"""

    @classmethod
    def setUpClass(cls):
        """Set up test client and performance thresholds"""
        cls.app = app.test_client()
        cls.app.testing = True

        # Performance thresholds
        cls.SMALL_MAP_THRESHOLD = 2.0  # 2 seconds for small maps
        cls.MEDIUM_MAP_THRESHOLD = 10.0  # 10 seconds for medium maps
        cls.LARGE_MAP_THRESHOLD = 30.0  # 30 seconds for large maps
        cls.MEMORY_THRESHOLD = 200 * 1024 * 1024  # 200MB memory increase

    def setUp(self):
        """Set up test data"""
        self.small_map_data = self._create_map_data(10, 10)
        self.medium_map_data = self._create_map_data(25, 25)
        self.large_map_data = self._create_map_data(50, 50)
        self.stress_map_data = self._create_complex_map_data(30, 30)

    def _create_map_data(self, width, height):
        """Create test map data with specified dimensions"""
        image_data = []
        for y in range(height):
            for x in range(width):
                # Create regions with some complexity
                region_x = x // 5
                region_y = y // 5

                # Add borders between regions
                if x % 5 == 0 or y % 5 == 0:
                    pixel = 0  # Border
                else:
                    # Region coloring based on position
                    pixel = 255 if (region_x + region_y) % 2 == 0 else 128

                image_data.extend([pixel, pixel, pixel, 255])

        return {
            "image": image_data,
            "width": width,
            "height": height,
            "userId": f"perf-test-{width}x{height}",
        }

    def _create_complex_map_data(self, width, height):
        """Create complex map data with many regions"""
        image_data = []
        for y in range(height):
            for x in range(width):
                # Create many small regions
                region_size = 3
                region_x = x // region_size
                region_y = y // region_size

                # Complex pattern with many boundaries
                if x % region_size == 0 or y % region_size == 0:
                    pixel = 0
                else:
                    # Create different regions
                    seed = (region_x * 7 + region_y * 11) % 3
                    pixel = [255, 128, 64][seed]

                image_data.extend([pixel, pixel, pixel, 255])

        return {
            "image": image_data,
            "width": width,
            "height": height,
            "userId": f"complex-test-{width}x{height}",
        }

    def test_small_map_performance(self):
        """Test performance with small maps (10x10)"""
        start_time = time.time()

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.small_map_data),
            content_type="application/json",
        )

        end_time = time.time()
        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)
        self.assertLess(
            processing_time,
            self.SMALL_MAP_THRESHOLD,
            f"Small map took {processing_time:.2f}s, expected < {self.SMALL_MAP_THRESHOLD}s",
        )

        print(f"Small map (10x10) processing time: {processing_time:.2f}s")

    def test_medium_map_performance(self):
        """Test performance with medium maps (25x25)"""
        start_time = time.time()

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.medium_map_data),
            content_type="application/json",
        )

        end_time = time.time()
        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)
        self.assertLess(
            processing_time,
            self.MEDIUM_MAP_THRESHOLD,
            f"Medium map took {processing_time:.2f}s, expected < {self.MEDIUM_MAP_THRESHOLD}s",
        )

        print(f"Medium map (25x25) processing time: {processing_time:.2f}s")

    def test_large_map_performance(self):
        """Test performance with large maps (50x50)"""
        start_time = time.time()

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.large_map_data),
            content_type="application/json",
        )

        end_time = time.time()
        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)
        self.assertLess(
            processing_time,
            self.LARGE_MAP_THRESHOLD,
            f"Large map took {processing_time:.2f}s, expected < {self.LARGE_MAP_THRESHOLD}s",
        )

        print(f"Large map (50x50) processing time: {processing_time:.2f}s")

    def test_memory_usage_performance(self):
        """Test memory usage during processing"""
        process = psutil.Process(os.getpid())
        initial_memory = process.memory_info().rss

        # Process multiple maps to test memory stability
        for i in range(5):
            test_data = self._create_map_data(20, 20)
            response = self.app.post(
                "/api/solve",
                data=json.dumps(test_data),
                content_type="application/json",
            )
            self.assertEqual(response.status_code, 200)

        final_memory = process.memory_info().rss
        memory_increase = final_memory - initial_memory

        self.assertLess(
            memory_increase,
            self.MEMORY_THRESHOLD,
            f"Memory increased by {memory_increase / 1024 / 1024:.2f}MB, "
            f"expected < {self.MEMORY_THRESHOLD / 1024 / 1024:.2f}MB",
        )

        print(f"Memory increase after 5 maps: {memory_increase / 1024 / 1024:.2f}MB")

    def test_concurrent_request_performance(self):
        """Test performance under concurrent load"""
        num_threads = 5
        results = []
        errors = []
        times = []

        def make_request():
            try:
                start_time = time.time()
                response = self.app.post(
                    "/api/solve",
                    data=json.dumps(self.small_map_data),
                    content_type="application/json",
                )
                end_time = time.time()

                results.append(response.status_code)
                times.append(end_time - start_time)
            except Exception as e:
                errors.append(str(e))

        # Start concurrent requests
        threads = []
        start_time = time.time()

        for _ in range(num_threads):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()

        # Wait for completion
        for thread in threads:
            thread.join()

        total_time = time.time() - start_time

        # Verify all succeeded
        self.assertEqual(len(errors), 0, f"Errors occurred: {errors}")
        self.assertEqual(len(results), num_threads)

        # Check individual and total performance
        max_individual_time = max(times)
        avg_individual_time = sum(times) / len(times)

        self.assertLess(
            max_individual_time,
            self.SMALL_MAP_THRESHOLD * 2,
            f"Slowest concurrent request took {max_individual_time:.2f}s",
        )
        self.assertLess(
            total_time,
            self.SMALL_MAP_THRESHOLD * 3,
            f"Total concurrent processing took {total_time:.2f}s",
        )

        print(
            f"Concurrent requests - Total: {total_time:.2f}s, "
            f"Average: {avg_individual_time:.2f}s, Max: {max_individual_time:.2f}s"
        )

    def test_algorithm_scalability(self):
        """Test how algorithm performance scales with input size"""
        sizes = [(5, 5), (10, 10), (15, 15), (20, 20), (25, 25)]
        times = []

        for width, height in sizes:
            test_data = self._create_map_data(width, height)

            start_time = time.time()
            response = self.app.post(
                "/api/solve",
                data=json.dumps(test_data),
                content_type="application/json",
            )
            end_time = time.time()

            self.assertEqual(response.status_code, 200)
            processing_time = end_time - start_time
            times.append((width * height, processing_time))

            print(
                f"Size {width}x{height} ({width*height} pixels): {processing_time:.3f}s"
            )

        # Check that time doesn't grow exponentially
        for i in range(1, len(times)):
            size_ratio = times[i][0] / times[i - 1][0]
            time_ratio = times[i][1] / times[i - 1][1] if times[i - 1][1] > 0 else 1

            # Time growth should be reasonable (not exponential)
            self.assertLess(
                time_ratio,
                size_ratio * 3,
                f"Performance degraded too much from size {times[i-1][0]} to {times[i][0]}",
            )

    def test_graph_coloring_performance(self):
        """Test graph coloring algorithm performance directly"""
        # Test with different graph sizes
        graph_sizes = [10, 50, 100, 200]

        for num_vertices in graph_sizes:
            # Create a moderately dense graph
            edges = []
            for i in range(num_vertices):
                for j in range(
                    i + 1, min(i + 6, num_vertices)
                ):  # Connect to next 5 vertices
                    edges.append((i, j))

            start_time = time.time()
            solution = solve_graph_csp(num_vertices, edges)
            end_time = time.time()

            processing_time = end_time - start_time

            self.assertIsNotNone(solution)
            self.assertEqual(len(solution), num_vertices)

            # Performance should be reasonable
            expected_time = num_vertices * 0.01  # 0.01s per vertex as rough estimate
            self.assertLess(
                processing_time,
                max(expected_time, 5.0),
                f"Graph coloring for {num_vertices} vertices took {processing_time:.3f}s",
            )

            print(f"Graph coloring {num_vertices} vertices: {processing_time:.3f}s")

    def test_vertex_detection_performance(self):
        """Test vertex detection algorithm performance"""
        sizes = [(20, 20), (30, 30), (40, 40), (50, 50)]

        for width, height in sizes:
            # Create binary image with multiple regions
            image = np.zeros((height, width))
            for y in range(height):
                for x in range(width):
                    if (x // 5 + y // 5) % 2 == 0 and x % 5 != 0 and y % 5 != 0:
                        image[y, x] = 1

            start_time = time.time()
            vertices, black, vertice_matrix = get_vertices(image.copy())
            end_time = time.time()

            processing_time = end_time - start_time

            # Should complete in reasonable time
            expected_time = (width * height) / 10000  # Rough estimate
            self.assertLess(
                processing_time,
                max(expected_time, 2.0),
                f"Vertex detection for {width}x{height} took {processing_time:.3f}s",
            )

            print(
                f"Vertex detection {width}x{height}: {processing_time:.3f}s, "
                f"found {len(vertices)} vertices"
            )

    def test_edge_detection_performance(self):
        """Test edge detection algorithm performance"""
        # Create test data with known number of regions
        image = np.zeros((30, 30))
        for i in range(0, 30, 6):
            for j in range(0, 30, 6):
                if i + 3 < 30 and j + 3 < 30:
                    image[i : i + 3, j : j + 3] = 1

        vertices, black, vertice_matrix = get_vertices(image.copy())

        start_time = time.time()
        edges = find_edges(black, vertices, vertice_matrix)
        end_time = time.time()

        processing_time = end_time - start_time

        # Edge detection should be reasonably fast
        self.assertLess(
            processing_time,
            5.0,
            f"Edge detection took {processing_time:.3f}s for {len(vertices)} vertices",
        )

        print(
            f"Edge detection: {processing_time:.3f}s for {len(vertices)} vertices, "
            f"found {len(edges)} edges"
        )

    def test_throughput_performance(self):
        """Test request throughput over time"""
        num_requests = 20
        start_time = time.time()

        successful_requests = 0
        for i in range(num_requests):
            response = self.app.post(
                "/api/solve",
                data=json.dumps(self.small_map_data),
                content_type="application/json",
            )
            if response.status_code == 200:
                successful_requests += 1

        end_time = time.time()
        total_time = end_time - start_time
        throughput = successful_requests / total_time

        # Should handle at least 2 requests per second for small maps
        self.assertGreater(
            throughput, 2.0, f"Throughput too low: {throughput:.2f} requests/second"
        )

        print(
            f"Throughput: {throughput:.2f} requests/second ({successful_requests}/{num_requests} successful)"
        )

    def test_complex_map_performance(self):
        """Test performance with complex maps (many small regions)"""
        start_time = time.time()

        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.stress_map_data),
            content_type="application/json",
        )

        end_time = time.time()
        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)

        # Complex maps should still complete in reasonable time
        self.assertLess(
            processing_time,
            20.0,
            f"Complex map took {processing_time:.2f}s, expected < 20s",
        )

        print(f"Complex map (30x30 with many regions): {processing_time:.2f}s")

    def test_worst_case_performance(self):
        """Test performance with worst-case scenario"""
        # Create checkerboard pattern (worst case for region detection)
        width, height = 20, 20
        image_data = []

        for y in range(height):
            for x in range(width):
                # Checkerboard creates maximum number of regions
                pixel = 255 if (x + y) % 2 == 0 else 0
                image_data.extend([pixel, pixel, pixel, 255])

        worst_case_data = {
            "image": image_data,
            "width": width,
            "height": height,
            "userId": "worst-case-test",
        }

        start_time = time.time()
        response = self.app.post(
            "/api/solve",
            data=json.dumps(worst_case_data),
            content_type="application/json",
        )
        end_time = time.time()

        processing_time = end_time - start_time

        self.assertEqual(response.status_code, 200)

        # Even worst case should complete in reasonable time
        self.assertLess(
            processing_time,
            15.0,
            f"Worst case took {processing_time:.2f}s, expected < 15s",
        )

        print(f"Worst case (checkerboard 20x20): {processing_time:.2f}s")

    def test_csp_solver_performance(self):
        """Test CSP solver performance with different graph complexities"""
        test_cases = [
            # (num_vertices, density_factor, description)
            (10, 0.3, "sparse_small"),
            (20, 0.3, "sparse_medium"),
            (10, 0.8, "dense_small"),
            (20, 0.5, "medium_density"),
            (30, 0.3, "sparse_large"),
        ]

        for num_vertices, density, description in test_cases:
            # Generate edges based on density
            edges = []
            max_edges = num_vertices * (num_vertices - 1) // 2
            target_edges = int(max_edges * density)

            import random

            random.seed(42)  # For reproducible results

            all_possible_edges = [
                (i, j) for i in range(num_vertices) for j in range(i + 1, num_vertices)
            ]
            edges = random.sample(
                all_possible_edges, min(target_edges, len(all_possible_edges))
            )

            csp = GraphColoringCSP(num_vertices, edges)

            start_time = time.time()
            solution = csp.solve()
            end_time = time.time()

            processing_time = end_time - start_time

            self.assertIsNotNone(solution)
            self.assertEqual(len(solution), num_vertices)

            # Performance expectations based on complexity
            if num_vertices <= 10:
                max_time = 1.0
            elif num_vertices <= 20:
                max_time = 5.0
            else:
                max_time = 10.0

            self.assertLess(
                processing_time,
                max_time,
                f"CSP solver for {description} took {processing_time:.3f}s, expected < {max_time}s",
            )

            print(
                f"CSP {description} ({num_vertices}v, {len(edges)}e): {processing_time:.3f}s"
            )

    def test_memory_efficiency(self):
        """Test memory efficiency during processing"""
        import gc

        # Force garbage collection before test
        gc.collect()
        process = psutil.Process(os.getpid())
        initial_memory = process.memory_info().rss

        # Process several maps of increasing size
        sizes = [(10, 10), (20, 20), (30, 30)]
        memory_measurements = []

        for width, height in sizes:
            test_data = self._create_map_data(width, height)

            # Measure memory before request
            before_memory = process.memory_info().rss

            response = self.app.post(
                "/api/solve",
                data=json.dumps(test_data),
                content_type="application/json",
            )

            # Measure memory after request
            after_memory = process.memory_info().rss

            self.assertEqual(response.status_code, 200)

            memory_increase = after_memory - before_memory
            memory_measurements.append((width * height, memory_increase))

            # Force garbage collection
            gc.collect()

        # Check that memory usage doesn't grow excessively
        for pixels, memory_increase in memory_measurements:
            memory_per_pixel = memory_increase / pixels if pixels > 0 else 0

            # Should use less than 1KB per pixel on average
            self.assertLess(
                memory_per_pixel,
                1024,
                f"Memory usage too high: {memory_per_pixel:.2f} bytes/pixel",
            )

            print(
                f"Memory for {pixels} pixels: {memory_increase / 1024:.2f}KB "
                f"({memory_per_pixel:.2f} bytes/pixel)"
            )

    def test_response_time_consistency(self):
        """Test that response times are consistent across multiple requests"""
        num_requests = 10
        response_times = []

        for i in range(num_requests):
            start_time = time.time()
            response = self.app.post(
                "/api/solve",
                data=json.dumps(self.small_map_data),
                content_type="application/json",
            )
            end_time = time.time()

            self.assertEqual(response.status_code, 200)
            response_times.append(end_time - start_time)

        # Calculate statistics
        avg_time = sum(response_times) / len(response_times)
        min_time = min(response_times)
        max_time = max(response_times)

        # Check consistency - max shouldn't be more than 3x average
        self.assertLess(
            max_time,
            avg_time * 3,
            f"Response time inconsistent: max {max_time:.3f}s vs avg {avg_time:.3f}s",
        )

        # All responses should be within reasonable bounds
        for i, time_taken in enumerate(response_times):
            self.assertLess(
                time_taken,
                self.SMALL_MAP_THRESHOLD,
                f"Request {i} took {time_taken:.3f}s",
            )

        print(
            f"Response time consistency - Avg: {avg_time:.3f}s, "
            f"Min: {min_time:.3f}s, Max: {max_time:.3f}s"
        )

    def test_cpu_usage_efficiency(self):
        """Test CPU usage during processing"""
        # This test monitors CPU usage patterns
        cpu_percentages = []

        def monitor_cpu():
            for _ in range(50):  # Monitor for 5 seconds
                cpu_percentages.append(psutil.cpu_percent(interval=0.1))

        # Start CPU monitoring in background
        import threading

        monitor_thread = threading.Thread(target=monitor_cpu)
        monitor_thread.start()

        # Process a medium-complexity map
        response = self.app.post(
            "/api/solve",
            data=json.dumps(self.medium_map_data),
            content_type="application/json",
        )

        # Wait for monitoring to complete
        monitor_thread.join()

        self.assertEqual(response.status_code, 200)

        if cpu_percentages:
            avg_cpu = sum(cpu_percentages) / len(cpu_percentages)
            max_cpu = max(cpu_percentages)

            # CPU usage should be reasonable (not maxing out the system)
            self.assertLess(max_cpu, 95.0, f"CPU usage too high: {max_cpu}%")

            print(f"CPU usage - Average: {avg_cpu:.1f}%, Max: {max_cpu:.1f}%")

    def test_algorithm_complexity_scaling(self):
        """Test how different algorithm components scale"""
        # Test vertex detection scaling
        vertex_times = []
        edge_times = []
        coloring_times = []

        sizes = [(10, 10), (15, 15), (20, 20), (25, 25)]

        for width, height in sizes:
            # Create test image
            image = np.zeros((height, width))
            for y in range(height):
                for x in range(width):
                    if (x // 3 + y // 3) % 2 == 0 and x % 3 != 0 and y % 3 != 0:
                        image[y, x] = 1

            # Time vertex detection
            start_time = time.time()
            vertices, black, vertice_matrix = get_vertices(image.copy())
            vertex_time = time.time() - start_time
            vertex_times.append((width * height, vertex_time))

            # Time edge detection
            start_time = time.time()
            edges = find_edges(black, vertices, vertice_matrix)
            edge_time = time.time() - start_time
            edge_times.append((len(vertices), edge_time))

            # Time graph coloring
            start_time = time.time()
            solution = solve_graph_csp(len(vertices), edges)
            coloring_time = time.time() - start_time
            coloring_times.append((len(vertices), coloring_time))

        # Analyze scaling for each component
        components = [
            ("Vertex Detection", vertex_times),
            ("Edge Detection", edge_times),
            ("Graph Coloring", coloring_times),
        ]

        for name, times in components:
            if len(times) >= 2:
                # Check that scaling is reasonable (not exponential)
                for i in range(1, len(times)):
                    size_ratio = (
                        times[i][0] / times[i - 1][0] if times[i - 1][0] > 0 else 1
                    )
                    time_ratio = (
                        times[i][1] / times[i - 1][1] if times[i - 1][1] > 0 else 1
                    )

                    # Time shouldn't grow faster than quadratic
                    self.assertLess(
                        time_ratio,
                        size_ratio**2,
                        f"{name} scaling too poor: {time_ratio:.2f}x time for {size_ratio:.2f}x size",
                    )

                print(
                    f"{name} scaling: {[f'{size}:{time:.3f}s' for size, time in times]}"
                )

    def test_load_balancing_simulation(self):
        """Simulate load balancing scenarios"""
        # Simulate different types of concurrent loads
        scenarios = [
            ("light_load", 3, self.small_map_data),
            ("medium_load", 5, self.small_map_data),
            ("heavy_load", 3, self.medium_map_data),
            ("mixed_load", 4, None),  # Mixed small and medium maps
        ]

        for scenario_name, num_threads, map_data in scenarios:
            results = []
            errors = []
            times = []

            def make_request(request_id):
                try:
                    if scenario_name == "mixed_load":
                        # Alternate between small and medium maps
                        test_data = (
                            self.small_map_data
                            if request_id % 2 == 0
                            else self.medium_map_data
                        )
                    else:
                        test_data = map_data

                    start_time = time.time()
                    response = self.app.post(
                        "/api/solve",
                        data=json.dumps(test_data),
                        content_type="application/json",
                    )
                    end_time = time.time()

                    results.append(response.status_code)
                    times.append(end_time - start_time)
                except Exception as e:
                    errors.append(str(e))

            # Execute scenario
            threads = []
            start_time = time.time()

            for i in range(num_threads):
                thread = threading.Thread(target=make_request, args=(i,))
                threads.append(thread)
                thread.start()

            for thread in threads:
                thread.join()

            total_time = time.time() - start_time

            # Verify results
            self.assertEqual(len(errors), 0, f"Errors in {scenario_name}: {errors}")
            self.assertEqual(len(results), num_threads)

            # Calculate metrics
            avg_response_time = sum(times) / len(times) if times else 0
            max_response_time = max(times) if times else 0
            throughput = len(results) / total_time if total_time > 0 else 0

            print(
                f"{scenario_name}: {throughput:.2f} req/s, "
                f"avg: {avg_response_time:.2f}s, max: {max_response_time:.2f}s"
            )

            # Performance assertions based on scenario
            if "light" in scenario_name:
                self.assertGreater(throughput, 1.0)
            elif "heavy" in scenario_name:
                self.assertLess(max_response_time, self.MEDIUM_MAP_THRESHOLD * 2)


if __name__ == "__main__":
    # Add performance test markers
    unittest.TestLoader.testMethodPrefix = "test_"
    unittest.main(verbosity=2)
