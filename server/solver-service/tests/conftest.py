import pytest
import sys
import os
from unittest.mock import patch

# Add the parent directory to the path to import the app
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import app


@pytest.fixture
def client():
    """Create a test client for the Flask app"""
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


@pytest.fixture
def mock_logger():
    """Mock the logger service to avoid external dependencies"""
    with patch("app.log_event") as mock_log:
        yield mock_log


@pytest.fixture
def sample_request_data():
    """Provide sample request data for tests"""
    return {
        "image": [255, 255, 255, 255] * 4,  # 2x2 white image
        "width": 2,
        "height": 2,
        "userId": "test-user",
    }


@pytest.fixture
def complex_request_data():
    """Provide complex request data for tests"""
    # Create a 10x10 image with multiple regions
    image_data = []
    for y in range(10):
        for x in range(10):
            # Create regions
            if x < 5 and y < 5:
                pixel = 255  # Top-left region
            elif x >= 5 and y < 5:
                pixel = 128  # Top-right region
            elif x < 5 and y >= 5:
                pixel = 64  # Bottom-left region
            else:
                pixel = 32  # Bottom-right region

            image_data.extend([pixel, pixel, pixel, 255])

    return {
        "image": image_data,
        "width": 10,
        "height": 10,
        "userId": "complex-test-user",
    }


@pytest.fixture(scope="session", autouse=True)
def setup_test_environment():
    """Set up test environment variables"""
    os.environ["LOGGER_SERVICE_URL"] = "localhost:50001"
    os.environ["PORT"] = "8082"


@pytest.fixture
def performance_data():
    """Provide data for performance testing"""
    return {
        "small": {
            "image": [255, 0] * 50,  # 5x5 checkerboard
            "width": 5,
            "height": 5,
            "userId": "perf-small",
        },
        "medium": {
            "image": [
                255 if (i // 4) % 2 == ((i // 4) // 20) % 2 else 0
                for i in range(20 * 20 * 4)
            ],
            "width": 20,
            "height": 20,
            "userId": "perf-medium",
        },
        "large": {
            "image": [255 if (i // 4) % 3 == 0 else 0 for i in range(50 * 50 * 4)],
            "width": 50,
            "height": 50,
            "userId": "perf-large",
        },
    }
