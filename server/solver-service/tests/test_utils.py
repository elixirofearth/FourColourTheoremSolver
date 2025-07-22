import numpy as np
import json


class SolverTestUtils:
    """Utility class for solver service tests"""

    @staticmethod
    def create_simple_map(width, height, pattern="regions"):
        """Create a simple test map with specified pattern"""
        image_data = []

        for y in range(height):
            for x in range(width):
                if pattern == "regions":
                    # Create 4 quadrant regions
                    if x < width // 2 and y < height // 2:
                        pixel = 255  # Top-left
                    elif x >= width // 2 and y < height // 2:
                        pixel = 128  # Top-right
                    elif x < width // 2 and y >= height // 2:
                        pixel = 64  # Bottom-left
                    else:
                        pixel = 32  # Bottom-right

                elif pattern == "checkerboard":
                    pixel = 255 if (x + y) % 2 == 0 else 0

                elif pattern == "stripes":
                    pixel = 255 if x % 2 == 0 else 0

                elif pattern == "solid":
                    pixel = 255

                elif pattern == "empty":
                    pixel = 0

                else:
                    pixel = 128  # Default gray

                # RGBA format
                image_data.extend([pixel, pixel, pixel, 255])

        return {
            "image": image_data,
            "width": width,
            "height": height,
            "userId": f"test-{pattern}-{width}x{height}",
        }

    @staticmethod
    def create_binary_image_array(width, height, pattern="random"):
        """Create a binary numpy array for algorithm testing"""
        image = np.zeros((height, width))

        if pattern == "random":
            np.random.seed(42)  # For reproducible tests
            image = np.random.choice([0, 1], size=(height, width))

        elif pattern == "checkerboard":
            for y in range(height):
                for x in range(width):
                    image[y, x] = 1 if (x + y) % 2 == 0 else 0

        elif pattern == "regions":
            # Create distinct regions
            for y in range(height):
                for x in range(width):
                    if x < width // 2 and y < height // 2:
                        image[y, x] = 1  # Top-left region
                    elif x >= width // 2 and y >= height // 2:
                        image[y, x] = 1  # Bottom-right region

        elif pattern == "single":
            # Single large region in center
            start_x, end_x = width // 4, 3 * width // 4
            start_y, end_y = height // 4, 3 * height // 4
            image[start_y:end_y, start_x:end_x] = 1

        elif pattern == "borders":
            # Create regions with clear borders
            region_size = min(width, height) // 3
            for i in range(0, width, region_size):
                for j in range(0, height, region_size):
                    if (i // region_size + j // region_size) % 2 == 0:
                        end_i = min(i + region_size - 1, width)
                        end_j = min(j + region_size - 1, height)
                        image[j:end_j, i:end_i] = 1

        return image

    @staticmethod
    def validate_colored_map(colored_map, expected_width, expected_height):
        """Validate the structure of a colored map response"""
        # Check basic structure
        assert isinstance(colored_map, list), "Colored map should be a list"
        assert (
            len(colored_map) == expected_height
        ), f"Expected height {expected_height}, got {len(colored_map)}"

        if expected_height > 0:
            assert (
                len(colored_map[0]) == expected_width
            ), f"Expected width {expected_width}, got {len(colored_map[0])}"

            # Check pixel structure
            for y in range(expected_height):
                assert len(colored_map[y]) == expected_width, f"Row {y} has wrong width"
                for x in range(expected_width):
                    pixel = colored_map[y][x]
                    assert isinstance(
                        pixel, list
                    ), f"Pixel at ({x},{y}) should be a list"
                    assert (
                        len(pixel) == 3
                    ), f"Pixel at ({x},{y}) should be RGB (3 values)"

                    # Check RGB values are valid
                    for i, channel in enumerate(pixel):
                        assert isinstance(
                            channel, (int, float)
                        ), f"Channel {i} at ({x},{y}) should be numeric"
                        assert (
                            0 <= channel <= 255
                        ), f"Channel {i} at ({x},{y}) should be 0-255, got {channel}"

    @staticmethod
    def count_unique_colors(colored_map):
        """Count unique colors in a colored map (excluding black borders)"""
        unique_colors = set()

        for row in colored_map:
            for pixel in row:
                color = tuple(int(c) for c in pixel)
                if color != (0, 0, 0):  # Exclude black borders
                    unique_colors.add(color)

        return len(unique_colors), unique_colors

    @staticmethod
    def get_expected_colors():
        """Get the expected color palette from the solver"""
        return {
            (255, 0, 0),  # Red
            (0, 255, 0),  # Green
            (0, 0, 255),  # Blue
            (255, 255, 0),  # Yellow
            (0, 0, 0),  # Black (borders)
        }

    @staticmethod
    def create_stress_test_data(width, height, complexity="medium"):
        """Create data for stress testing"""
        if complexity == "low":
            # Few large regions
            region_size = max(width, height) // 3
        elif complexity == "medium":
            # Medium-sized regions
            region_size = max(width, height) // 6
        elif complexity == "high":
            # Many small regions
            region_size = max(width, height) // 12
        else:
            region_size = max(width, height) // 6

        image_data = []
        for y in range(height):
            for x in range(width):
                # Create regions based on position
                region_x = x // region_size
                region_y = y // region_size

                # Add borders between regions
                if x % region_size == 0 or y % region_size == 0:
                    pixel = 0  # Border
                else:
                    # Alternate regions
                    pixel = 255 if (region_x + region_y) % 2 == 0 else 128

                image_data.extend([pixel, pixel, pixel, 255])

        return {
            "image": image_data,
            "width": width,
            "height": height,
            "userId": f"stress-{complexity}-{width}x{height}",
        }

    @staticmethod
    def create_malicious_payload(attack_type="oversized"):
        """Create malicious payloads for security testing"""
        if attack_type == "oversized":
            return {
                "image": [255] * (1000 * 1000 * 4),
                "width": 1000,
                "height": 1000,
                "userId": "oversized-attack",
            }

        elif attack_type == "type_confusion":
            return {
                "image": "not_an_array",
                "width": "not_a_number",
                "height": {"nested": "object"},
                "userId": ["array", "instead", "of", "string"],
            }

        elif attack_type == "injection":
            return {
                "image": [255] * 16,
                "width": 2,
                "height": 2,
                "userId": "'; DROP TABLE users; --",
            }

        elif attack_type == "overflow":
            return {
                "image": [255] * 16,
                "width": 2**31 - 1,
                "height": 2**31 - 1,
                "userId": "overflow-attack",
            }

        elif attack_type == "unicode":
            return {
                "image": [255] * 16,
                "width": 2,
                "height": 2,
                "userId": "user\u202eadmin\u0000test",
            }

        else:
            return {
                "image": [255] * 16,
                "width": 2,
                "height": 2,
                "userId": "generic-attack",
            }

    @staticmethod
    def validate_four_color_theorem(colored_map, adjacency_info=None):
        """Validate that the coloring satisfies the four-color theorem"""
        height = len(colored_map)
        if height == 0:
            return True

        width = len(colored_map[0])

        # Create color map for easy comparison
        color_map = {}
        for y in range(height):
            for x in range(width):
                color = tuple(int(c) for c in colored_map[y][x])
                color_map[(x, y)] = color

        # Check adjacent pixels don't have the same color (excluding black borders)
        for y in range(height):
            for x in range(width):
                current_color = color_map[(x, y)]

                # Skip black pixels (borders)
                if current_color == (0, 0, 0):
                    continue

                # Check 4-connected neighbors
                neighbors = [(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)]

                for nx, ny in neighbors:
                    if 0 <= nx < width and 0 <= ny < height:
                        neighbor_color = color_map[(nx, ny)]

                        # Skip black pixels (borders)
                        if neighbor_color == (0, 0, 0):
                            continue

                        # Adjacent non-border pixels should have different colors
                        if current_color == neighbor_color:
                            return False

        return True

    @staticmethod
    def measure_performance_metrics(colored_map, processing_time):
        """Calculate performance metrics for a solved map"""
        if not colored_map:
            return {}

        height = len(colored_map)
        width = len(colored_map[0]) if height > 0 else 0
        total_pixels = width * height

        # Count colors used
        num_colors, unique_colors = SolverTestUtils.count_unique_colors(colored_map)

        # Calculate pixels per second
        pixels_per_second = total_pixels / processing_time if processing_time > 0 else 0

        return {
            "width": width,
            "height": height,
            "total_pixels": total_pixels,
            "processing_time": processing_time,
            "pixels_per_second": pixels_per_second,
            "colors_used": num_colors,
            "unique_colors": unique_colors,
            "satisfies_four_color_theorem": SolverTestUtils.validate_four_color_theorem(
                colored_map
            ),
        }

    @staticmethod
    def create_edge_case_data():
        """Create various edge case test data"""
        edge_cases = {
            "minimal": SolverTestUtils.create_simple_map(1, 1, "solid"),
            "thin_vertical": SolverTestUtils.create_simple_map(1, 10, "stripes"),
            "thin_horizontal": SolverTestUtils.create_simple_map(10, 1, "stripes"),
            "empty": SolverTestUtils.create_simple_map(5, 5, "empty"),
            "solid": SolverTestUtils.create_simple_map(5, 5, "solid"),
            "checkerboard": SolverTestUtils.create_simple_map(8, 8, "checkerboard"),
            "large_sparse": SolverTestUtils.create_simple_map(100, 100, "regions"),
        }

        # Add zero dimension cases
        edge_cases["zero_width"] = {
            "image": [],
            "width": 0,
            "height": 5,
            "userId": "zero-width-test",
        }

        edge_cases["zero_height"] = {
            "image": [],
            "width": 5,
            "height": 0,
            "userId": "zero-height-test",
        }

        return edge_cases

    @staticmethod
    def analyze_algorithm_complexity(vertices, edges, solution_time):
        """Analyze the algorithmic complexity of a solved problem"""
        num_vertices = len(vertices) if vertices else 0
        num_edges = len(edges) if edges else 0

        # Calculate graph density
        max_edges = num_vertices * (num_vertices - 1) // 2 if num_vertices > 1 else 0
        density = num_edges / max_edges if max_edges > 0 else 0

        # Estimate complexity based on graph properties
        if num_vertices <= 10:
            expected_time_category = "instant"  # < 0.1s
        elif num_vertices <= 50:
            expected_time_category = "fast"  # < 1s
        elif num_vertices <= 200:
            expected_time_category = "medium"  # < 10s
        else:
            expected_time_category = "slow"  # < 30s

        return {
            "num_vertices": num_vertices,
            "num_edges": num_edges,
            "graph_density": density,
            "solution_time": solution_time,
            "expected_time_category": expected_time_category,
            "edges_per_vertex": num_edges / num_vertices if num_vertices > 0 else 0,
        }


# Test data constants
SAMPLE_SMALL_MAP = SolverTestUtils.create_simple_map(5, 5, "regions")
SAMPLE_MEDIUM_MAP = SolverTestUtils.create_simple_map(20, 20, "regions")
SAMPLE_LARGE_MAP = SolverTestUtils.create_simple_map(50, 50, "regions")

EXPECTED_COLORS = SolverTestUtils.get_expected_colors()

# Performance benchmarks
PERFORMANCE_THRESHOLDS = {
    "small_map_time": 2.0,  # seconds
    "medium_map_time": 10.0,  # seconds
    "large_map_time": 30.0,  # seconds
    "memory_limit": 200 * 1024 * 1024,  # 200MB
    "throughput_min": 2.0,  # requests per second
}
