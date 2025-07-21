from flask import Flask, jsonify, request
from flask_cors import CORS
from dotenv import load_dotenv

load_dotenv()
import os
import skimage as ski
from skimage import io, segmentation, color
import matplotlib.pyplot as plt
import numpy as np
from skimage.morphology import binary_dilation, square
import time
import json
import grpc
from google.protobuf.timestamp_pb2 import Timestamp
from datetime import datetime
import proto.logs.logger_pb2 as logger_pb2
import proto.logs.logger_pb2_grpc as logger_pb2_grpc
from collections import defaultdict, deque
import random

# app instance
app = Flask(__name__)
CORS(app)

# Add logging configuration
LOGGER_URL = "logger-service:50001"  # gRPC service address


def log_event(user_id, event_type, description, severity=1, metadata=None):
    try:
        # Create gRPC channel
        channel = grpc.insecure_channel(LOGGER_URL)
        stub = logger_pb2_grpc.LoggerServiceStub(channel)

        # Create timestamp
        timestamp = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")

        # Create log request
        request = logger_pb2.LogRequest(
            service_name="map_coloring_service",
            event_type=event_type,
            user_id=user_id,
            description=description,
            severity=severity,
            timestamp=timestamp,
            metadata=metadata or {},
        )

        # Make gRPC call
        response = stub.LogEvent(request)
        print(f"Log event response: {response}")

        if not response.success:
            print(f"Failed to log event: {response.message}")

    except Exception as e:
        print(f"Error logging event: {str(e)}")


@app.route("/", methods=["GET"])
def return_home():
    return jsonify(
        {"message": "Hello World!!!", "people": ["Sheikh", "Riley", "Peter", "Andy"]}
    )


@app.route("/api/solve", methods=["POST"])
def solve():
    try:

        data = request.get_json()
        if not data:
            return jsonify({"error": "No JSON data received"}), 400

        # Print the data
        print("width: ", data["width"])
        print("height: ", data["height"])
        print("user id: ", data["userId"])

        user_id = data.get("userId", "unknown")

        if "image" not in data or "width" not in data or "height" not in data:
            return jsonify({"error": "Missing required fields"}), 400

        # Convert image data to integers if they're strings
        image_data = data["image"]
        if isinstance(image_data[0], str):
            image_data = [int(x) for x in image_data]

        width = int(data["width"])
        height = int(data["height"])

        # Convert image data to numpy array
        index = 0
        array = np.zeros((height, width))

        for y in range(height):
            for x in range(width):
                try:
                    pixel_value = int(image_data[index])
                    array[y][x] = 1 if pixel_value > 128 else 0
                except (ValueError, TypeError) as e:
                    return (
                        jsonify({"error": f"Invalid pixel data at index {index}"}),
                        400,
                    )
                index += 4

        begin = time.time()

        vertices, black, vertice_matrix = get_vertices(array)
        edges = find_edges(array, vertices, vertice_matrix)
        solution = solve_graph_csp(len(vertices), edges)
        colored_map = color_map(vertices, solution, black)

        end = time.time()
        processing_time = end - begin

        print(f"Processing time: {processing_time:.2f} seconds")

        log_event(
            user_id,
            "map_coloring_completed",
            "Successfully colored map",
            1,
            {
                "processing_time": f"{processing_time:.2f}",
                "vertices": str(len(vertices)),
                "edges": str(len(edges)),
            },
        )

        # Convert numpy array to list for JSON serialization
        result = colored_map.tolist()
        return jsonify(result), 200

    except Exception as e:
        print(f"Error processing request: {str(e)}")
        import traceback

        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


# Add a health check endpoint
@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy"})


def preprocess_image(image):
    # remove alpha channel
    # if image.shape[2] == 4:
    #   image = color.rgba2rgb(image)
    # make greyscale
    image = color.rgb2gray(image)
    return image


def get_vertices(image):
    # find all uncolored chunks of the map
    vertices = []
    num = 0
    seed_point = (0, 0)
    # find size of image
    height, width = image.shape[:2]
    vertice_matrix = image
    for x in range(width):
        for y in range(height):
            if image[y, x] == 1:
                num += 1
                # find the chunk associated with a vertex
                vertex = segmentation.flood(image, (y, x))
                vertice_matrix[vertex] = num
                vertices.append(vertex)
                # remove the chunk from the map
                image = segmentation.flood_fill(image, (y, x), 0)

    return vertices, image, vertice_matrix


def find_edges(image, vertices, vertice_matrix):
    # find all adjacecies between countries
    edges = list()
    num_vertices = len(vertices)
    # fuzzyness is how far countries are allowed to be apart to still be considered as bordering each other
    fuzzyness = 8
    # find neighbours for each country
    start = time.time()
    for i in range(num_vertices):
        # expand countries size to check overlapping
        dilated_image = binary_dilation(vertices[i], footprint=square(fuzzyness))
        height, width = dilated_image.shape[:2]

        vertice_matrix_copy = vertice_matrix.copy()
        vertice_matrix_copy[np.logical_not(dilated_image)] = 0
        adjacents = np.unique(vertice_matrix_copy)
        for adjacent in adjacents:
            if adjacent != 0 and adjacent != (i + 1):
                edges.append((i, int(adjacent - 1)))

        """
        # check each possible other country
        for j in range((i + 1), num_vertices):
            # take only the overlap between the enlarged country and its neighbour
            overlap = np.minimum(dilated_image, vertices[j])
            # check if there is any overlap
            all_zeros = np.all(overlap == 0)
            # if adjacent add an edge
            if  (all_zeros == False):
                edges.append((i, j))
        """

    end = time.time()

    print(end - start)
    return edges


class GraphColoringCSP:
    def __init__(self, num_vertices, edges):
        self.num_vertices = num_vertices
        self.colors = ["red", "green", "blue", "yellow"]
        self.adjacency_list = defaultdict(list)

        # Build adjacency list from edges
        for u, v in edges:
            self.adjacency_list[u].append(v)
            self.adjacency_list[v].append(u)

        # Initialize domains and assignment
        self.domains = {i: self.colors.copy() for i in range(num_vertices)}
        self.assignment = {}

    def is_consistent(self, vertex, color):
        """Check if assigning color to vertex is consistent with current assignment"""
        for neighbor in self.adjacency_list[vertex]:
            if neighbor in self.assignment and self.assignment[neighbor] == color:
                return False
        return True

    def forward_check(self, vertex, color):
        """Apply forward checking - remove conflicting values from neighboring domains"""
        removed_values = defaultdict(list)

        for neighbor in self.adjacency_list[vertex]:
            if neighbor not in self.assignment and color in self.domains[neighbor]:
                self.domains[neighbor].remove(color)
                removed_values[neighbor].append(color)

                # If any domain becomes empty, this is not viable
                if not self.domains[neighbor]:
                    return None, removed_values

        return True, removed_values

    def restore_domains(self, removed_values):
        """Restore removed values to domains (for backtracking)"""
        for vertex, colors in removed_values.items():
            self.domains[vertex].extend(colors)

    def select_unassigned_variable(self):
        """Choose next variable using MRV (Minimum Remaining Values) heuristic"""
        unassigned = [v for v in range(self.num_vertices) if v not in self.assignment]
        if not unassigned:
            return None

        # Choose variable with smallest domain
        return min(unassigned, key=lambda v: len(self.domains[v]))

    def order_domain_values(self, vertex):
        """Order domain values using LCV (Least Constraining Value) heuristic"""

        def count_conflicts(color):
            conflicts = 0
            for neighbor in self.adjacency_list[vertex]:
                if neighbor not in self.assignment and color in self.domains[neighbor]:
                    conflicts += 1
            return conflicts

        return sorted(self.domains[vertex], key=count_conflicts)

    def backtrack_search(self):
        """Main backtracking search algorithm with constraint propagation"""
        if len(self.assignment) == self.num_vertices:
            return self.assignment

        vertex = self.select_unassigned_variable()
        if vertex is None:
            return self.assignment

        for color in self.order_domain_values(vertex):
            if self.is_consistent(vertex, color):
                # Make assignment
                self.assignment[vertex] = color
                original_domain = self.domains[vertex].copy()
                self.domains[vertex] = [color]

                # Apply forward checking
                fc_result, removed_values = self.forward_check(vertex, color)

                if fc_result is not None:  # No domain became empty
                    result = self.backtrack_search()
                    if result:
                        return result

                # Backtrack
                del self.assignment[vertex]
                self.domains[vertex] = original_domain
                self.restore_domains(removed_values)

        return None

    def solve(self):
        """Solve the graph coloring problem"""
        result = self.backtrack_search()
        if result:
            return {str(k): v for k, v in result.items()}
        else:
            # Fallback to greedy coloring if CSP fails
            return self.greedy_coloring()

    def greedy_coloring(self):
        """Fallback greedy coloring algorithm"""
        coloring = {}
        vertices = list(range(self.num_vertices))
        # Shuffle for better average performance
        random.shuffle(vertices)

        for vertex in vertices:
            used_colors = set()
            for neighbor in self.adjacency_list[vertex]:
                if neighbor in coloring:
                    used_colors.add(coloring[neighbor])

            # Find first available color
            for color in self.colors:
                if color not in used_colors:
                    coloring[vertex] = color
                    break

        return {str(k): v for k, v in coloring.items()}


def solve_graph_csp(num_vertices, edges):
    """Solve graph coloring using constraint satisfaction approach"""
    if num_vertices == 0:
        return {}

    csp = GraphColoringCSP(num_vertices, edges)
    solution = csp.solve()

    print(f"Solved graph with {num_vertices} vertices and {len(edges)} edges")
    return solution


def color_map(vertices, solution, black):
    image = black
    image = color.gray2rgb(image)
    for i in range(len(vertices)):
        mask = vertices[i]
        vertices[i] = color.gray2rgb(vertices[i])
        colored = solution[str(i)]
        if colored == "green":
            new_color = (0, 255, 0)
        elif colored == "blue":
            new_color = (0, 0, 255)
        elif colored == "red":
            new_color = (255, 0, 0)
        else:
            new_color = (255, 255, 0)
        vertices[i][mask] = new_color
        image = np.maximum(image, vertices[i])
    # io.imsave("image.png", image)
    return image


if __name__ == "__main__":
    port = os.getenv("PORT")
    app.run(port=(port or 1000))
