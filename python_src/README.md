# FPTD - Python Implementation

Python implementation of the paper: **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"**

## Overview

FPTD is a privacy-preserving truth discovery system for crowdsensing applications. It uses secure multi-party computation (MPC) based on Shamir's secret sharing scheme to protect user privacy while aggregating crowdsourced data.

## Project Structure

```
python_src/
├── fptd/
│   ├── __init__.py
│   ├── main.py                 # Main entry point
│   ├── params.py               # Global parameters
│   ├── share.py                # Secret share data structure
│   ├── edge_server.py          # Server communication
│   ├── sharing/
│   │   └── shamir_sharing.py   # Shamir secret sharing
│   ├── protocols/
│   │   ├── gate.py             # Base gate class
│   │   ├── circuit.py          # Circuit class
│   │   ├── input_gate.py       # Input gate
│   │   ├── output_gate.py      # Output gate
│   │   ├── dot_product_gate.py # Dot product gate
│   │   ├── division_gate.py    # Division gate
│   │   └── ...                 # Other gates
│   ├── offline/
│   │   ├── fake_party.py       # Offline data generator
│   │   ├── offline_circuit.py  # Offline circuit
│   │   └── offline_gate.py     # Offline gates
│   ├── truth_discovery/
│   │   ├── td_offline.py       # Offline phase
│   │   └── td_online.py        # Online phase
│   └── utils/
│       ├── data_manager.py     # Data management
│       ├── linear_algebra.py   # Linear algebra utilities
│       └── tool.py             # Common tools
├── setup.py
├── requirements.txt
└── README.md
```

## Requirements

- Python >= 3.8
- No external dependencies (uses only Python standard library)

## Installation

```bash
# Install as package
cd python_src
pip install -e .
```

## Usage

### Run with default dataset

```bash
cd python_src
python -m fptd.main
```

### Use as library

```python
from fptd.params import Params
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

Edit `fptd/params.py` to modify parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `NUM_SERVER` | 7 | Number of servers |
| `N` | 7 | Number of parties |
| `T` | 4 | Threshold for secret sharing |
| `ITER_TD` | 3 | Truth discovery iterations |
| `PRECISE_ROUND` | 100000 | Fixed-point precision |

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

## Core Components

### Secret Sharing
- **Shamir's (t,n) threshold scheme**: Requires t shares to reconstruct the secret

### Secure Computation Gates
- **Linear gates**: Add, Subtract, Scale (no communication)
- **Multiplication gates**: Dot product, Element-wise multiply (using Beaver triples)
- **Division gate**: Secure truncation protocol
- **Logarithm gate**: Taylor series approximation

### Truth Discovery Algorithm
1. Initialize truth estimates with simple average
2. For each iteration:
   - Compute distances between worker answers and estimated truth
   - Calculate worker weights based on distances
   - Update truth estimates using weighted average
3. Output final truth estimates

## License

See the LICENSE file in the root directory.

## Reference

This is the Python implementation of the Java source code from the paper "FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing".
