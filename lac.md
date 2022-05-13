```shell
git clone https://github.com/PaddlePaddle/Paddle.git

# 选择其中一个稳定的分支
cd Paddle
git checkout v1.8.5

# 创建并进入build目录
mkdir build
cd build

# 编译结果保存路径，需要需改
PADDLE_ROOT=/path/of/paddle
export CC=/path/of/gcc-8
export CXX=/path/of/g++-8

# 编译运行
cmake -DFLUID_INFERENCE_INSTALL_DIR=$PADDLE_ROOT \
-DCMAKE_BUILD_TYPE=Release \
-DWITH_PYTHON=OFF \
-DWITH_MKL=ON \
-DWITH_GPU=OFF  \
-DON_INFER=ON \
../

make
make inference_lib_dist
```

```shell
# 代码下载
git clone https://github.com/baidu/lac.git
cd lac

# /path/to/paddle是第1步中获取的Paddle依赖库路径
# 即下载解压后的文件夹路径或编译产出的文件夹路径
PADDLE_ROOT=/path/of/paddle

# JAVA的HOME目录，应存在文件${JAVA_HOME}/include/jni.h
JAVA_HOME=/path/of/java

# 修改 CMakeLists.txt, 设置 cc, cxx
# SET(CMAKE_C_COMPILER "/usr/bin/cc-8")                                                                                                         
# SET(CMAKE_CXX_COMPILER "/usr/bin/c++-8") 

# 编译
mkdir build 
cd build
cmake -DPADDLE_ROOT=$PADDLE_ROOT \
      -DJAVA_HOME=$JAVA_HOME \
      -DWITH_JNILIB=ON \
      -DWITH_DEMO=OFF \
      ../

make install # 编译产出在 ../output/java 下
```
