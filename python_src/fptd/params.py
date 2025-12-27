"""
全局参数配置
"""

class Params:
    # 服务器数量
    NUM_SERVER = 7
    
    # Shamir秘密共享参数
    N = 7  # 参与方数量
    T = 4  # 门限值 (N/2 + 1)
    
    # 真值发现迭代次数
    ITER_TD = 3
    
    # 有限域素数 (512位)
    P = int(
        "686479766013060971498190079908139321726943530014330540939446345918554318339765605212255964066145455497729631139148085803712198799971664381257402855770649"
        "18191753095488370057523717020191526968292064814554920952659462848194611371602116714684652611658559090067375008469446209401503990128824814548817698963319"
        "28686613"
    )
    
    # 定点数精度
    PRECISE_ROUND = 100000
    
    # 数据文件路径
    SENSING_DATA_FILE = "datasets/weather/answer.csv"
    TRUTH_FILE = "datasets/weather/truth.csv"
    IS_CATEGORICAL_DATA = False
    
    # 是否打印执行信息
    IS_PRINT_EXE_INFO = True
    
    # 离线数据目录
    OFFLINE_DATA_DIR = "./offline_data"
    
    # 网络配置
    BASE_PORT = 8000
    HOST = "localhost"
    
    # 除法门参数
    DIV_L = 64      # 除数位长
    DIV_E = 90      # 被除数位长
    DIV_SIGMA = 64  # 安全参数
