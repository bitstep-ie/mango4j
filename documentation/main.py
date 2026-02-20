import os

def define_env(env):
    env.variables["mango4j_latest_version"] = os.getenv(
        "MANGO4J_LATEST_VERSION"
    ) or "unknown"