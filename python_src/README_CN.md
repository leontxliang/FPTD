# FPTD - Python 实现

论文 **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"** 的 Python 实现。

## 项目概述

FPTD 是一个面向众包感知应用的隐私保护真值发现系统。本实现使用 NumPy 向量化操作高效计算 CRH (Conflict Resolution on Heterogeneous Data) 算法。

## 项目结构

```
python_src/
├── fptd/
│   ├── __init__.py
│   ├── main.py                 # 主程序入口
│   ├── params.py               # 全局参数配置
│   ├── truth_discovery/
│   │   ├── td_offline.py       # 离线阶段 (MPC 协议)
│   │   └── td_online.py        # 在线阶段 (CRH 算法)
│   └── utils/
│       ├── data_manager.py     # 数据加载与评估
│       └── tool.py             # 通用工具
├── setup.py
├── requirements.txt
└── README.md
```

## 环境要求

- Python >= 3.8
- NumPy

## 安装

```bash
cd python_src
pip install -e .
```

## 使用方法

### 命令行

```bash
python -m fptd.main
python -m fptd.main -i 5      # 5 次迭代
python -m fptd.main -q        # 安静模式
```

### 作为库使用

```python
from fptd.utils.data_manager import DataManager
from fptd.truth_discovery.td_online import run_truth_discovery

# 加载数据
data_manager = DataManager(
    "datasets/weather/answer.csv",
    "datasets/weather/truth.csv"
)

# 运行真值发现
predictions = run_truth_discovery(data_manager)

# 评估结果
rmse, mae = data_manager.evaluate_predictions(predictions)
print(f"RMSE: {rmse:.4f}, MAE: {mae:.4f}")
```

## 参数配置

编辑 `fptd/params.py`：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `ITER_TD` | 3 | 迭代次数 |
| `IS_PRINT_EXE_INFO` | True | 打印执行信息 |

## 数据集格式

### 感知数据 (answer.csv)

```csv
question,worker,answer
11,1,151
12,1,135
14,2,153
```

### 真实值 (truth.csv)

```csv
question,truth
2,130.0
3,90.0
23,81.5
```

## 算法说明

CRH 算法迭代执行：
1. 使用简单平均初始化真值估计
2. 计算工人答案与估计真值之间的距离
3. 计算工人权重：`weight[w] = log(sum_all_distance / distance[w])`
4. 使用加权平均更新真值估计
5. 重复步骤 2-4 指定次数

## 运行示例

```
============================================================
FPTD - Fast Privacy-Preserving Truth Discovery
============================================================

Dataset statistics:
  Number of workers: 9
  Number of exams: 1400
  Iterations: 3

Running Truth Discovery...
Iteration 1/3 completed
Iteration 2/3 completed
Iteration 3/3 completed

Results:
  RMSE: 29.9243
  MAE: 29.2416

============================================================
FPTD completed successfully!
============================================================
```

## 许可证

请参阅根目录下的 LICENSE 文件。
