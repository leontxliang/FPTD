# FPTD - Python 实现

论文 **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"** 的 Python 实现。

## 项目概述

FPTD 是一个面向众包感知应用的隐私保护真值发现系统。它使用基于 Shamir 秘密共享方案的安全多方计算 (MPC) 技术，在聚合众包数据的同时保护用户隐私。

## 项目结构

```
python_src/
├── fptd/
│   ├── __init__.py
│   ├── main.py                 # 主程序入口
│   ├── params.py               # 全局参数配置
│   ├── share.py                # 秘密份额数据结构
│   ├── edge_server.py          # 服务器通信
│   ├── sharing/
│   │   └── shamir_sharing.py   # Shamir 秘密共享
│   ├── protocols/
│   │   ├── gate.py             # 门电路基类
│   │   ├── circuit.py          # 电路类
│   │   ├── input_gate.py       # 输入门
│   │   ├── output_gate.py      # 输出门
│   │   ├── dot_product_gate.py # 点积门
│   │   ├── division_gate.py    # 除法门
│   │   └── ...                 # 其他门
│   ├── offline/
│   │   ├── fake_party.py       # 离线数据生成器
│   │   ├── offline_circuit.py  # 离线电路
│   │   └── offline_gate.py     # 离线门
│   ├── truth_discovery/
│   │   ├── td_offline.py       # 离线阶段
│   │   └── td_online.py        # 在线阶段
│   └── utils/
│       ├── data_manager.py     # 数据管理
│       ├── linear_algebra.py   # 线性代数工具
│       └── tool.py             # 通用工具
├── setup.py
├── requirements.txt
└── README.md
```

## 环境要求

- Python >= 3.8
- 无外部依赖（仅使用 Python 标准库）

## 安装

```bash
# 以包的形式安装
cd python_src
pip install -e .
```

## 使用方法

### 使用默认数据集运行

```bash
cd python_src
python -m fptd.main
```

### 作为库使用

```python
from fptd.params import Params
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

编辑 `fptd/params.py` 修改参数：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `NUM_SERVER` | 7 | 服务器数量 |
| `N` | 7 | 参与方数量 |
| `T` | 4 | 秘密共享门限值 |
| `ITER_TD` | 3 | 真值发现迭代次数 |
| `PRECISE_ROUND` | 100000 | 定点数精度 |

## 数据集格式

### 感知数据 (answer.csv)

```csv
question,worker,answer
11,1,151
12,1,135
14,2,153
```

- `question`: 问题/任务 ID
- `worker`: 工人 ID
- `answer`: 工人提交的答案

### 真实值 (truth.csv)

```csv
question,truth
2,130.0
3,90.0
23,81.5
```

- `question`: 问题 ID
- `truth`: 真实答案

## 核心组件

### 秘密共享
- **Shamir (t,n) 门限方案**：需要至少 t 个份额才能恢复秘密

### 安全计算门
- **线性门**：加法、减法、缩放（无通信开销）
- **乘法门**：点积、逐元素乘法（使用 Beaver 三元组）
- **除法门**：安全截断协议
- **对数门**：泰勒级数近似

### 真值发现算法

1. 使用简单平均初始化真值估计
2. 对于每次迭代：
   - 计算工人答案与估计真值之间的距离
   - 根据距离计算工人权重
   - 使用加权平均更新真值估计
3. 输出最终真值估计

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

## 参考

本项目是论文 "FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing" 中 Java 源代码的 Python 实现。
