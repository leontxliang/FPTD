# FPTD

论文 **"FPTD: Super Fast Privacy-Preserving and Reliable Truth Discovery for Crowdsensing"** 的源代码。

## 项目概述

FPTD 是一个面向众包感知应用的隐私保护真值发现系统。它使用基于 Shamir 秘密共享方案的安全多方计算 (MPC) 技术，在聚合众包数据的同时保护用户隐私。

## 项目结构

```
FPTD/
├── src/                        # Java 源代码 (MPC 实现)
│   ├── main/java/fptd/
│   │   ├── Main.java           # 主程序入口
│   │   ├── Params.java         # 全局参数配置
│   │   ├── EdgeServer.java     # 服务器通信
│   │   ├── ServerThread.java   # 多线程服务器
│   │   ├── Share.java          # 秘密份额数据结构
│   │   ├── sharing/            # 秘密共享方案
│   │   ├── protocols/          # MPC 协议门电路
│   │   ├── offline/            # 离线阶段 (预处理)
│   │   ├── truthDiscovery/     # 真值发现算法
│   │   └── utils/              # 工具类
│   └── test/java/              # 单元测试
├── python_src/                 # Python 实现
├── datasets/                   # 示例数据集
└── README.md
```

## 环境要求

- Java >= 11
- 无外部依赖

## 使用方法

### 运行默认数据集

```bash
cd src/main/java
javac fptd/Main.java
java fptd.Main
```

或使用 IDE (IntelliJ IDEA, Eclipse) 运行 `fptd.Main`。

### 参数配置

编辑 `src/main/java/fptd/Params.java`：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `NUM_SERVER` | 7 | 服务器数量 |
| `N` | 7 | 参与方数量 |
| `T` | 4 | 秘密共享门限值 |
| `ITER_TD` | 3 | 真值发现迭代次数 |
| `PRECISE_ROUND` | 100000 | 定点数精度 |
| `sensingDataFile` | datasets/weather/answer.csv | 输入数据文件 |
| `truthFile` | datasets/weather/truth.csv | 真实值文件 |

### 切换数据集

在 `Params.java` 中取消注释所需的数据集：

```java
// 天气数据集 (连续型)
public final static String sensingDataFile = "datasets/weather/answer.csv";
public final static String truthFile = "datasets/weather/truth.csv";
public final static boolean isCategoricalData = false;

// 鸭子识别数据集 (分类型)
// public final static String sensingDataFile = "datasets/d_Duck_Identification/answer.csv";
// public final static String truthFile = "datasets/d_Duck_Identification/truth.csv";
// public final static boolean isCategoricalData = true;
```

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

## 系统架构

### 两阶段协议

1. **离线阶段** (`TDOfflineOptimal.java`)
   - 生成 Beaver 三元组和随机份额
   - 与实际输入数据无关
   - 可预先计算

2. **在线阶段** (`TDOnlineOptimal.java`)
   - 构建计算电路
   - 使用预处理数据执行安全计算
   - 输出聚合的真值

### 核心组件

- **Shamir 秘密共享**：(t,n) 门限方案，需要 t 个份额才能恢复秘密
- **安全门电路**：加法、减法、乘法、除法、对数
- **多线程执行**：每个服务器在独立线程中运行

### 真值发现算法 (CRH)

1. 使用简单平均初始化真值估计
2. 对于每次迭代：
   - 计算工人答案与估计真值之间的距离
   - 计算工人权重：`weight[w] = log(sum_all_distance / distance[w])`
   - 使用加权平均更新真值估计
3. 输出最终真值估计

## 重要说明

- `TDOfflineOptimal.java` 中的电路拓扑结构必须与 `TDOnlineOptimal.java` 完全一致
- 已在配备 Apple M3 芯片的 MacBook Air 上测试通过

## Python 实现

`python_src/` 目录下提供了简化的 Python 实现，详见 `python_src/README.md`。

## 许可证

请参阅 LICENSE 文件。
