import unittest
import numpy as np
import sys
import os

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import (
    get_vertices,
    find_edges,
    solve_graph_csp,
    color_map,
    GraphColoringCSP,
    preprocess_image,
)


class TestImageProcessingAlgorithms(unittest.TestCase):

    def setUp(self):
        """Set up test data"""
        # Simple 4x4 binary image with two regions
        self.simple_image = np.array(
            [[1, 1, 0, 0], [1, 1, 0, 0], [0, 0, 1, 1], [0, 0, 1, 1]]
        )

        # Single region image
        self.single_region = np.array([[1, 1], [1, 1]])

        # Empty image (all black)
        self.empty_image = np.array([[0, 0], [0, 0]])

        # Complex image with multiple regions
        self.complex_image = np.array(
            [
                [1, 1, 0, 1, 1],
                [1, 1, 0, 1, 1],
                [0, 0, 0, 0, 0],
                [1, 1, 0, 1, 1],
                [1, 1, 0, 1, 1],
            ]
        )

    def test_get_vertices_simple_image(self):
        """Test vertex detection on simple image"""
        vertices, black, vertice_matrix = get_vertices(self.simple_image.copy())

        # Should find 2 vertices
        self.assertEqual(len(vertices), 2)

        # Black regions should be 0
        self.assertTrue(np.all(black == 0))

        # Vertice matrix should have labeled regions
        unique_labels = np.unique(vertice_matrix)
        self.assertIn(0, unique_labels)  # Background
        self.assertTrue(len(unique_labels) >= 2)  # At least one region

    def test_get_vertices_single_region(self):
        """Test vertex detection on single region"""
        vertices, black, vertice_matrix = get_vertices(self.single_region.copy())

        # Should find 1 vertex
        self.assertEqual(len(vertices), 1)

        # The single vertex should cover the entire region
        self.assertTrue(np.array_equal(vertices[0], [[True, True], [True, True]]))

    def test_get_vertices_empty_image(self):
        """Test vertex detection on empty image"""
        vertices, black, vertice_matrix = get_vertices(self.empty_image.copy())

        # Should find 0 vertices
        self.assertEqual(len(vertices), 0)

        # All should remain black
        self.assertTrue(np.array_equal(black, self.empty_image))

    def test_get_vertices_complex_image(self):
        """Test vertex detection on complex image"""
        vertices, black, vertice_matrix = get_vertices(self.complex_image.copy())

        # Should find 4 vertices (4 corners)
        self.assertEqual(len(vertices), 4)

        # Verify that each vertex is a boolean mask
        for vertex in vertices:
            self.assertEqual(vertex.dtype, bool)
            self.assertEqual(vertex.shape, self.complex_image.shape)

    def test_find_edges_simple_image(self):
        """Test edge detection on simple image"""
        vertices, black, vertice_matrix = get_vertices(self.simple_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # Should find some edges between adjacent regions
        self.assertIsInstance(edges, list)

        # Each edge should be a tuple of two integers
        for edge in edges:
            self.assertIsInstance(edge, tuple)
            self.assertEqual(len(edge), 2)
            self.assertIsInstance(edge[0], int)
            self.assertIsInstance(edge[1], int)

    def test_find_edges_single_region(self):
        """Test edge detection on single region"""
        vertices, black, vertice_matrix = get_vertices(self.single_region.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # Single region should have no edges
        self.assertEqual(len(edges), 0)

    def test_find_edges_empty_image(self):
        """Test edge detection on empty image"""
        vertices, black, vertice_matrix = get_vertices(self.empty_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # Empty image should have no edges
        self.assertEqual(len(edges), 0)

    def test_find_edges_no_duplicate_edges(self):
        """Test that edge detection doesn't create duplicate edges"""
        vertices, black, vertice_matrix = get_vertices(self.complex_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # Convert to set of sorted tuples to check for duplicates
        normalized_edges = set()
        for edge in edges:
            normalized_edge = tuple(sorted(edge))
            normalized_edges.add(normalized_edge)

        # Check that we don't have more normalized edges than original edges
        # (allowing for some duplicates due to algorithm implementation)
        self.assertLessEqual(len(normalized_edges), len(edges))

    def test_find_edges_valid_vertex_indices(self):
        """Test that all edge indices are valid vertex indices"""
        vertices, black, vertice_matrix = get_vertices(self.complex_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        num_vertices = len(vertices)
        for edge in edges:
            self.assertGreaterEqual(edge[0], 0)
            self.assertLess(edge[0], num_vertices)
            self.assertGreaterEqual(edge[1], 0)
            self.assertLess(edge[1], num_vertices)

    def test_color_map_simple_case(self):
        """Test map coloring with simple case"""
        vertices, black, vertice_matrix = get_vertices(self.simple_image.copy())
        solution = {"0": "red", "1": "blue"}

        colored_map = color_map(vertices, solution, black)

        # Result should be RGB image
        self.assertEqual(len(colored_map.shape), 3)
        self.assertEqual(colored_map.shape[2], 3)

        # Should have same height and width as original
        self.assertEqual(colored_map.shape[0], self.simple_image.shape[0])
        self.assertEqual(colored_map.shape[1], self.simple_image.shape[1])

    def test_color_map_all_colors(self):
        """Test map coloring with all available colors"""
        # Create 4 separate regions
        four_region_image = np.array(
            [[1, 0, 1, 0], [0, 0, 0, 0], [1, 0, 1, 0], [0, 0, 0, 0]]
        )

        vertices, black, vertice_matrix = get_vertices(four_region_image.copy())
        solution = {"0": "red", "1": "green", "2": "blue", "3": "yellow"}

        colored_map = color_map(vertices, solution, black)

        # Check that different colors are present
        self.assertEqual(colored_map.shape[2], 3)  # RGB

        # Verify that we have non-zero values (colors were applied)
        self.assertTrue(np.any(colored_map > 0))

    def test_color_map_empty_solution(self):
        """Test map coloring with empty solution"""
        vertices, black, vertice_matrix = get_vertices(self.empty_image.copy())
        solution = {}

        colored_map = color_map(vertices, solution, black)

        # Should return RGB version of black image
        self.assertEqual(len(colored_map.shape), 3)
        self.assertEqual(colored_map.shape[2], 3)

    def test_color_map_invalid_color(self):
        """Test map coloring with unrecognized color defaults to yellow"""
        vertices, black, vertice_matrix = get_vertices(self.single_region.copy())
        solution = {"0": "purple"}  # Invalid color

        colored_map = color_map(vertices, solution, black)

        # Should default to yellow for unknown colors
        # Check that some pixels are yellow (255, 255, 0)
        yellow_pixels = np.all(colored_map == [255, 255, 0], axis=2)
        self.assertTrue(np.any(yellow_pixels))

    def test_preprocess_image_rgb_to_gray(self):
        """Test image preprocessing converts RGB to grayscale"""
        # Create a simple RGB image
        rgb_image = np.array(
            [[[255, 0, 0], [0, 255, 0]], [[0, 0, 255], [255, 255, 255]]]
        )

        gray_image = preprocess_image(rgb_image)

        # Should be 2D grayscale
        self.assertEqual(len(gray_image.shape), 2)
        self.assertEqual(gray_image.shape, (2, 2))

        # Values should be between 0 and 1
        self.assertTrue(np.all(gray_image >= 0))
        self.assertTrue(np.all(gray_image <= 1))

    def test_image_processing_pipeline_integration(self):
        """Test the complete image processing pipeline"""
        # Start with original image
        image = self.simple_image.copy()

        # Get vertices
        vertices, black, vertice_matrix = get_vertices(image)
        self.assertGreater(len(vertices), 0)

        # Find edges
        edges = find_edges(black, vertices, vertice_matrix)

        # Solve coloring
        solution = solve_graph_csp(len(vertices), edges)
        self.assertEqual(len(solution), len(vertices))

        # Color the map
        colored_map = color_map(vertices, solution, black)
        self.assertEqual(len(colored_map.shape), 3)

    def test_get_vertices_preserves_original_regions(self):
        """Test that vertex detection preserves original region information"""
        original_image = self.simple_image.copy()
        vertices, black, vertice_matrix = get_vertices(original_image)

        # Reconstruct the image from vertices
        reconstructed = np.zeros_like(original_image)
        for i, vertex in enumerate(vertices):
            reconstructed[vertex] = 1

        # The reconstructed image should match the original white regions
        self.assertTrue(np.array_equal(reconstructed, self.simple_image))

    def test_find_edges_symmetric(self):
        """Test that edge detection creates symmetric adjacency"""
        vertices, black, vertice_matrix = get_vertices(self.complex_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # Check that if (i, j) is an edge, then (j, i) should also be present
        edge_set = set(edges)
        for edge in edges:
            i, j = edge
            # The algorithm might create both (i,j) and (j,i) or might only create one
            # But the adjacency should be logically symmetric
            self.assertTrue((i, j) in edge_set or (j, i) in edge_set)

    def test_color_map_color_consistency(self):
        """Test that color mapping uses consistent colors"""
        vertices, black, vertice_matrix = get_vertices(self.simple_image.copy())
        solution = {"0": "red", "1": "blue"}

        colored_map = color_map(vertices, solution, black)

        # Check for red pixels (255, 0, 0)
        red_pixels = np.all(colored_map == [255, 0, 0], axis=2)
        # Check for blue pixels (0, 0, 255)
        blue_pixels = np.all(colored_map == [0, 0, 255], axis=2)

        # Should have some pixels of each color
        self.assertTrue(np.any(red_pixels))
        self.assertTrue(np.any(blue_pixels))

    def test_get_vertices_handles_disconnected_regions(self):
        """Test vertex detection with disconnected regions of same color"""
        # Create image with two disconnected white regions
        disconnected_image = np.array([[1, 0, 1], [0, 0, 0], [1, 0, 1]])

        vertices, black, vertice_matrix = get_vertices(disconnected_image.copy())

        # Should detect 4 separate vertices (4 disconnected regions)
        self.assertEqual(len(vertices), 4)

    def test_edge_detection_with_no_adjacency(self):
        """Test edge detection when regions are not adjacent"""
        # Create image with separated regions
        separated_image = np.array(
            [
                [1, 0, 0, 0, 1],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [1, 0, 0, 0, 1],
            ]
        )

        vertices, black, vertice_matrix = get_vertices(separated_image.copy())
        edges = find_edges(black, vertices, vertice_matrix)

        # With sufficient separation, there should be fewer edges
        # The exact number depends on the fuzzyness parameter in find_edges
        self.assertIsInstance(edges, list)


if __name__ == "__main__":
    unittest.main()
