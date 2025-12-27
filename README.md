# FPTD

Source code of paper: **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"**

## Overview

FPTD is a privacy-preserving truth discovery system for crowdsensing applications. It uses secure multi-party computation (MPC) based on Shamir's secret sharing scheme to protect user privacy while aggregating crowdsourced data.

## Project Structure

```
FPTD/
├── src/                        # Java source code (MPC implementation)
│   ├── main/java/fptd/
│   │   ├── Main.java           # Main entry point
│   │   ├── Params.java         # Global parameters
│   │   ├── EdgeServer.java     # Server communication
│   │   ├── ServerThread.java   # Multi-threaded server
│   │   ├── Share.java          # Secret share data structure
│   │   ├── sharing/            # Secret sharing schemes
│   │   ├── protocols/          # MPC protocol gates
│   │   ├── offline/            # Offline phase (preprocessing)
│   │   ├── truthDiscovery/     # Truth discovery algorithms
│   │   └── utils/              # Utilities
│   └── test/java/              # Unit tests
├── python_src/                 # Python implementation
├── datasets/                   # Sample datasets
└── README.md
```

## Requirements

- Java >= 11
- No external dependencies

## Usage

### Run with default dataset

```bash
cd src/main/java
javac fptd/Main.java
java fptd.Main
```

Or use your IDE (IntelliJ IDEA, Eclipse) to run `fptd.Main`.

### Configuration

Edit `src/main/java/fptd/Params.java`:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `NUM_SERVER` | 7 | Number of servers |
| `N` | 7 | Number of parties |
| `T` | 4 | Threshold for secret sharing |
| `ITER_TD` | 3 | Truth discovery iterations |
| `PRECISE_ROUND` | 100000 | Fixed-point precision |
| `sensingDataFile` | datasets/weather/answer.csv | Input data file |
| `truthFile` | datasets/weather/truth.csv | Ground truth file |

### Switch Dataset

Uncomment the desired dataset in `Params.java`:

```java
// Weather dataset (continuous)
public final static String sensingDataFile = "datasets/weather/answer.csv";
public final static String truthFile = "datasets/weather/truth.csv";
public final static boolean isCategoricalData = false;

// Duck Identification dataset (categorical)
// public final static String sensingDataFile = "datasets/d_Duck_Identification/answer.csv";
// public final static String truthFile = "datasets/d_Duck_Identification/truth.csv";
// public final static boolean isCategoricalData = true;
```

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

## Architecture

### Two-Phase Protocol

1. **Offline Phase** (`TDOfflineOptimal.java`)
   - Generates Beaver triples and random shares
   - Independent of actual input data
   - Can be precomputed

2. **Online Phase** (`TDOnlineOptimal.java`)
   - Builds computation circuit
   - Executes secure computation using preprocessed data
   - Outputs aggregated truth values

### Core Components

- **Shamir Secret Sharing**: (t,n) threshold scheme requiring t shares to reconstruct
- **Secure Gates**: Add, Subtract, Multiply, Divide, Logarithm
- **Multi-threaded Execution**: Each server runs in a separate thread

### Truth Discovery Algorithm (CRH)

1. Initialize truth estimates with simple average
2. For each iteration:
   - Compute distances between worker answers and estimated truth
   - Calculate worker weights: `weight[w] = log(sum_all_distance / distance[w])`
   - Update truth estimates using weighted average
3. Output final truth estimates

## Important Notes

- The circuit topology in `TDOfflineOptimal.java` must match `TDOnlineOptimal.java`
- Tested on MacBook Air with Apple M3 chip

## Python Implementation

A simplified Python implementation is available in `python_src/`. See `python_src/README.md` for details.

## License

See the LICENSE file.
