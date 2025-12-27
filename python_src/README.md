# FPTD - Python Implementation

Python implementation of the paper: **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"**

## Overview

FPTD is a privacy-preserving truth discovery system for crowdsensing applications. This implementation uses NumPy vectorized operations for efficient computation of the CRH (Conflict Resolution on Heterogeneous Data) algorithm.

## Project Structure

```
python_src/
├── fptd/
│   ├── __init__.py
│   ├── main.py                 # Main entry point
│   ├── params.py               # Global parameters
│   ├── truth_discovery/
│   │   ├── td_offline.py       # Offline phase (MPC protocols)
│   │   └── td_online.py        # Online phase (CRH algorithm)
│   └── utils/
│       ├── data_manager.py     # Data loading and evaluation
│       └── tool.py             # Common utilities
├── setup.py
├── requirements.txt
└── README.md
```

## Requirements

- Python >= 3.8
- NumPy

## Installation

```bash
cd python_src
pip install -e .
```

## Usage

### Command Line

```bash
python -m fptd.main
python -m fptd.main -i 5      # 5 iterations
python -m fptd.main -q        # quiet mode
```

### As Library

```python
from fptd.utils.data_manager import DataManager
from fptd.truth_discovery.td_online import run_truth_discovery

# Load data
data_manager = DataManager(
    "datasets/weather/answer.csv",
    "datasets/weather/truth.csv"
)

# Run truth discovery
predictions = run_truth_discovery(data_manager)

# Evaluate
rmse, mae = data_manager.evaluate_predictions(predictions)
print(f"RMSE: {rmse:.4f}, MAE: {mae:.4f}")
```

## Configuration

Edit `fptd/params.py`:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `ITER_TD` | 3 | Number of iterations |
| `IS_PRINT_EXE_INFO` | True | Print execution info |

## Dataset Format

### Sensing data (answer.csv)

```csv
question,worker,answer
11,1,151
12,1,135
14,2,153
```

### Ground truth (truth.csv)

```csv
question,truth
2,130.0
3,90.0
23,81.5
```

## Algorithm

The CRH algorithm iteratively:
1. Initialize truth estimates with simple average
2. Compute distances between worker answers and estimated truth
3. Calculate worker weights: `weight[w] = log(sum_all_distance / distance[w])`
4. Update truth estimates using weighted average
5. Repeat steps 2-4 for specified iterations

## License

See the LICENSE file in the root directory.
