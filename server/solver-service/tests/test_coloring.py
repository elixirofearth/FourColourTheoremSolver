import unittest
import sys
import os
from collections import defaultdict

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import GraphColoringCSP, solve_graph_csp


class TestGraphColoringCSP(unittest.TestCase):

    def setUp(self):
        """Set up test data"""
        # Simple triangle graph (3 vertices, all connected)
        self.triangle_edges = [(0, 1), (1, 2), (0, 2)]

        # Path graph (3 vertices in a line)
        self.path_edges = [(0, 1), (1, 2)]

        # Complete graph on 4 vertices
        self.complete_4_edges = [(0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)]

        # Disconnected graph (two separate edges)
        self.disconnected_edges = [(0, 1), (2, 3)]

        # Empty graph (no edges)
        self.empty_edges = []

    def test_csp_initialization(self):
        """Test CSP initialization"""
        csp = GraphColoringCSP(3, self.triangle_edges)

        self.assertEqual(csp.num_vertices, 3)
        self.assertEqual(len(csp.colors), 4)
        self.assertIn("red", csp.colors)
        self.assertIn("green", csp.colors)
        self.assertIn("blue", csp.colors)
        self.assertIn("yellow", csp.colors)

        # Check adjacency list construction
        self.assertIn(1, csp.adjacency_list[0])
        self.assertIn(2, csp.adjacency_list[0])
        self.assertIn(0, csp.adjacency_list[1])
        self.assertIn(2, csp.adjacency_list[1])

    def test_is_consistent_valid_assignment(self):
        """Test consistency check with valid assignment"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        csp.assignment = {0: "red", 1: "blue"}

        # Vertex 2 can be green (different from neighbors)
        self.assertTrue(csp.is_consistent(2, "green"))

        # Vertex 2 cannot be red (same as vertex 0)
        self.assertFalse(csp.is_consistent(2, "red"))

        # Vertex 2 cannot be blue (same as vertex 1)
        self.assertFalse(csp.is_consistent(2, "blue"))

    def test_is_consistent_no_assignment(self):
        """Test consistency check with no existing assignments"""
        csp = GraphColoringCSP(3, self.triangle_edges)

        # Any color should be consistent for any vertex when no assignments exist
        self.assertTrue(csp.is_consistent(0, "red"))
        self.assertTrue(csp.is_consistent(1, "blue"))
        self.assertTrue(csp.is_consistent(2, "green"))

    def test_forward_check_success(self):
        """Test forward checking that succeeds"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        csp.assignment = {0: "red"}

        # Assign blue to vertex 1
        result, removed_values = csp.forward_check(1, "blue")

        self.assertIsNotNone(result)
        self.assertIsInstance(removed_values, defaultdict)

        # Red should be removed from neighbors' domains
        self.assertNotIn("blue", csp.domains[0])  # 0 is neighbor of 1
        self.assertNotIn("blue", csp.domains[2])  # 2 is neighbor of 1

    def test_forward_check_failure(self):
        """Test forward checking that fails"""
        csp = GraphColoringCSP(2, [(0, 1)])
        csp.assignment = {0: "red"}
        csp.domains[1] = ["red"]  # Only red available for vertex 1

        # Assigning red to vertex 0 should make vertex 1's domain empty
        result, removed_values = csp.forward_check(0, "red")

        self.assertIsNone(result)

    def test_restore_domains(self):
        """Test domain restoration after backtracking"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        original_domains = {v: csp.domains[v].copy() for v in csp.domains}

        # Perform forward check
        result, removed_values = csp.forward_check(0, "red")

        # Restore domains
        csp.restore_domains(removed_values)

        # Domains should be restored to original state
        for vertex in original_domains:
            self.assertEqual(set(csp.domains[vertex]), set(original_domains[vertex]))

    def test_select_unassigned_variable_mrv(self):
        """Test variable selection using MRV heuristic"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        csp.assignment = {0: "red"}

        # Reduce domain size for vertex 1
        csp.domains[1] = ["blue"]
        csp.domains[2] = ["blue", "green", "yellow"]

        # Should select vertex 1 (smallest domain)
        selected = csp.select_unassigned_variable()
        self.assertEqual(selected, 1)

    def test_select_unassigned_variable_all_assigned(self):
        """Test variable selection when all variables are assigned"""
        csp = GraphColoringCSP(2, [(0, 1)])
        csp.assignment = {0: "red", 1: "blue"}

        selected = csp.select_unassigned_variable()
        self.assertIsNone(selected)

    def test_order_domain_values_lcv(self):
        """Test domain value ordering using LCV heuristic"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        csp.assignment = {}

        # All neighbors have all colors available
        ordered_values = csp.order_domain_values(0)

        # Should return all colors (exact order may vary based on conflicts)
        self.assertEqual(len(ordered_values), 4)
        self.assertIn("red", ordered_values)
        self.assertIn("blue", ordered_values)
        self.assertIn("green", ordered_values)
        self.assertIn("yellow", ordered_values)

    def test_backtrack_search_triangle_graph(self):
        """Test backtracking search on triangle graph"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        solution = csp.backtrack_search()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # Verify solution validity
        for vertex in solution:
            for neighbor in csp.adjacency_list[vertex]:
                if neighbor in solution:
                    self.assertNotEqual(solution[vertex], solution[neighbor])

    def test_backtrack_search_path_graph(self):
        """Test backtracking search on path graph"""
        csp = GraphColoringCSP(3, self.path_edges)
        solution = csp.backtrack_search()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # Verify solution validity
        self.assertNotEqual(solution[0], solution[1])
        self.assertNotEqual(solution[1], solution[2])
        # Vertices 0 and 2 can have the same color (not adjacent)

    def test_backtrack_search_complete_graph(self):
        """Test backtracking search on complete graph (worst case)"""
        csp = GraphColoringCSP(4, self.complete_4_edges)
        solution = csp.backtrack_search()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 4)

        # Every vertex should have a different color
        colors_used = set(solution.values())
        self.assertEqual(len(colors_used), 4)

    def test_backtrack_search_disconnected_graph(self):
        """Test backtracking search on disconnected graph"""
        csp = GraphColoringCSP(4, self.disconnected_edges)
        solution = csp.backtrack_search()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 4)

        # Verify constraints
        self.assertNotEqual(solution[0], solution[1])
        self.assertNotEqual(solution[2], solution[3])

    def test_backtrack_search_empty_graph(self):
        """Test backtracking search on empty graph (no edges)"""
        csp = GraphColoringCSP(3, self.empty_edges)
        solution = csp.backtrack_search()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # All vertices can have the same color
        self.assertIn(solution[0], csp.colors)
        self.assertIn(solution[1], csp.colors)
        self.assertIn(solution[2], csp.colors)

    def test_solve_method_success(self):
        """Test the main solve method with solvable graph"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        solution = csp.solve()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # Keys should be strings
        for key in solution:
            self.assertIsInstance(key, str)

    def test_solve_method_fallback_to_greedy(self):
        """Test solve method fallback to greedy when CSP fails"""
        # Create an impossible graph (5 vertices all connected, only 4 colors)
        impossible_edges = [(i, j) for i in range(5) for j in range(i + 1, 5)]
        csp = GraphColoringCSP(5, impossible_edges)

        # This should fall back to greedy coloring
        solution = csp.solve()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 5)

    def test_greedy_coloring_simple(self):
        """Test greedy coloring algorithm"""
        csp = GraphColoringCSP(3, self.triangle_edges)
        solution = csp.greedy_coloring()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # Verify that no adjacent vertices have the same color
        for vertex_str, color in solution.items():
            vertex = int(vertex_str)
            for neighbor in csp.adjacency_list[vertex]:
                neighbor_str = str(neighbor)
                if neighbor_str in solution:
                    self.assertNotEqual(color, solution[neighbor_str])

    def test_greedy_coloring_empty_graph(self):
        """Test greedy coloring on empty graph"""
        csp = GraphColoringCSP(3, self.empty_edges)
        solution = csp.greedy_coloring()

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

    def test_solve_graph_csp_function(self):
        """Test the main solve_graph_csp function"""
        solution = solve_graph_csp(3, self.triangle_edges)

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 3)

        # Keys should be strings
        for key in solution:
            self.assertIsInstance(key, str)

    def test_solve_graph_csp_zero_vertices(self):
        """Test solve_graph_csp with zero vertices"""
        solution = solve_graph_csp(0, [])

        self.assertEqual(solution, {})

    def test_solve_graph_csp_single_vertex(self):
        """Test solve_graph_csp with single vertex"""
        solution = solve_graph_csp(1, [])

        self.assertEqual(len(solution), 1)
        self.assertIn("0", solution)

    def test_solve_graph_csp_large_graph(self):
        """Test solve_graph_csp with larger graph"""
        # Create a cycle graph with 10 vertices
        cycle_edges = [(i, (i + 1) % 10) for i in range(10)]

        solution = solve_graph_csp(10, cycle_edges)

        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), 10)

        # Verify solution validity
        for edge in cycle_edges:
            v1, v2 = str(edge[0]), str(edge[1])
            self.assertNotEqual(solution[v1], solution[v2])

    def test_csp_with_invalid_edges(self):
        """Test CSP handling of invalid edge indices"""
        # Edges with invalid vertex indices
        invalid_edges = [(0, 1), (1, 5)]  # Vertex 5 doesn't exist in 3-vertex graph

        csp = GraphColoringCSP(3, invalid_edges)

        # Should handle gracefully
        solution = csp.solve()
        self.assertIsNotNone(solution)

    def test_csp_consistency_with_partial_assignment(self):
        """Test consistency checking with partial assignment"""
        csp = GraphColoringCSP(4, [(0, 1), (1, 2), (2, 3)])
        csp.assignment = {0: "red", 2: "blue"}

        # Vertex 1 cannot be red (adjacent to 0) or blue (adjacent to 2)
        self.assertFalse(csp.is_consistent(1, "red"))
        self.assertFalse(csp.is_consistent(1, "blue"))
        self.assertTrue(csp.is_consistent(1, "green"))
        self.assertTrue(csp.is_consistent(1, "yellow"))

    def test_domain_reduction_effectiveness(self):
        """Test that domain reduction helps solve problems faster"""
        csp = GraphColoringCSP(4, self.complete_4_edges)

        # Initial domains should have all 4 colors
        for vertex in range(4):
            self.assertEqual(len(csp.domains[vertex]), 4)

        # After assigning one vertex, forward check should reduce domains
        csp.assignment = {0: "red"}
        result, removed_values = csp.forward_check(0, "red")

        # All neighbors should have red removed from their domains
        for neighbor in csp.adjacency_list[0]:
            self.assertNotIn("red", csp.domains[neighbor])

    def test_backtracking_with_impossible_assignment(self):
        """Test backtracking behavior with impossible assignment"""
        # Create graph that requires 5 colors but only 4 available
        impossible_edges = [(i, j) for i in range(5) for j in range(i + 1, 5)]
        csp = GraphColoringCSP(5, impossible_edges)

        # This should return None (impossible to solve with 4 colors)
        solution = csp.backtrack_search()
        self.assertIsNone(solution)

    def test_color_assignment_validity(self):
        """Test that all color assignments are from valid color set"""
        csp = GraphColoringCSP(5, [(0, 1), (1, 2), (2, 3), (3, 4)])
        solution = csp.solve()

        valid_colors = {"red", "green", "blue", "yellow"}
        for vertex, color in solution.items():
            self.assertIn(color, valid_colors)

    def test_solution_completeness(self):
        """Test that solution includes all vertices"""
        num_vertices = 6
        edges = [(0, 1), (1, 2), (2, 3), (3, 4), (4, 5)]

        solution = solve_graph_csp(num_vertices, edges)

        # Should have exactly num_vertices entries
        self.assertEqual(len(solution), num_vertices)

        # Should have all vertices from 0 to num_vertices-1
        expected_vertices = {str(i) for i in range(num_vertices)}
        actual_vertices = set(solution.keys())
        self.assertEqual(expected_vertices, actual_vertices)

    def test_edge_symmetry_handling(self):
        """Test that algorithm handles edge symmetry correctly"""
        # Test with both (0,1) and (1,0) edges
        symmetric_edges = [(0, 1), (1, 0), (1, 2)]

        csp = GraphColoringCSP(3, symmetric_edges)

        # Adjacency list should handle duplicates
        self.assertIn(1, csp.adjacency_list[0])
        self.assertIn(0, csp.adjacency_list[1])

        solution = csp.solve()
        self.assertIsNotNone(solution)

    def test_performance_with_dense_graph(self):
        """Test performance characteristics with dense graph"""
        import time

        # Create a moderately dense graph
        num_vertices = 8
        edges = [
            (i, j)
            for i in range(num_vertices)
            for j in range(i + 1, num_vertices)
            if (i + j) % 3 != 0
        ]

        start_time = time.time()
        solution = solve_graph_csp(num_vertices, edges)
        end_time = time.time()

        # Should complete in reasonable time (less than 5 seconds)
        self.assertLess(end_time - start_time, 5.0)
        self.assertIsNotNone(solution)
        self.assertEqual(len(solution), num_vertices)


if __name__ == "__main__":
    unittest.main()
