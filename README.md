esp32-camera-PTZ-V1
===
>
> 這專案是用 ESP32-CAM 板子做的一個簡單的雲台


## - ***CONTENT*** -

<br/>

## [HW （hardward）](#hw)
- 材料
- 建摩
- 接線

## [SW （softward）](#sw) 

- esp32-cam
- android

<br/>

##  [！！使用說明](#使用說明)

<br/>

---

# - ***HW*** -

> ### 材料

- [ ] esp32-cam
- [ ] Mg996r 180°
- [ ] Mg996r 360°
- [ ] HCSR04
- [ ] ANT （非必要）

> ### 建模
>
>在 `HW` 資料夾裡面有 `body.png` 、`bottom.stl`、`L.stl`、`top.stl` 共 5 個檔案，以下表示可檔案用途。

#### ` body.png ` 為下面這張圖片

![圖片](HW\body.png "img")

> 以上圖為例

- #### `top.stl` : 紫色的那塊

- #### ` bottom.stl ` : 澄色的那塊

- #### ` L.stl ` : 白色的那塊

> ### 接線

| ESP32-CAM | To |object
| :--:  | :--: | :--:
| GPIO 12 | S （bottom）| servo-motor
| GPIO 4 | S （top）| servo-motor
| GPIO  | TX | HCSR04
| GPIO  | RX | HCSR04

---

# - ***SW*** -

1. 包含了 esp32-cam 和 android 的程式，但不包含 ios
2. 編寫 esp32-cam 的環境是用 platfromio 這在 VScode 延伸模組可下載
3. android 試用 Android studio 在 SW 下有 android-app.apk 可以試試，但還有些Bug

> 延伸 [ 2 ]  : platformio.ini 設定如下
>
> - 注意導入的是 ESP32Servo 不是 Servo ，因為如果用 Servo 會出現 Bug
> - upload_port 記得要改
>
```ini
[env:esp32cam]
platform = espressif32
board = esp32cam
framework = arduino
upload_port = COM[6]
monitor_speed = 115200
lib_deps = 
 madhephaestus/ESP32Servo@^0.12.0
 espressif/esp32-camera@^2.0.0 
```

> # ***esp32-cam***

- 檔案在 `SW/esp32-cam/src/main.cpp`

## 1. 基本上就是設計一個接收控制和輸出影像的程式

首先在輸出影像的部分，主要是在 `camera_handler` 內，再來因為我是 `esp_https_server` 這個庫，所以在輸出和接收資料方面相對於其他庫會麻煩點。

> #### esp_https_server
>
> 是 esp32idef 的

再來是接收控制，代碼主要是在 ` cmd_handler ` 中，在這串代碼中最重要的是這串吧，

```cpp
    if (!strcmp(variable, "top")) {
      if (vertical_val >= 0) {
        vertical_val -= servo_move_val;
      }
        vertical_servo.write(vertical_val);
    }
    else if (!strcmp(variable, "bottom")) {
      if (vertical_val <= 180) {
        vertical_val += servo_move_val;
      }
        vertical_servo.write(vertical_val);
    }
    else if (!strcmp(variable, "leftT")) {
        horizontal_servo.write(servo_left_W);
    }
    else if (!strcmp(variable, "leftF")) {
        horizontal_servo.write(servo_stop_W);
    }
    else if (!strcmp(variable, "rightT")) {
        horizontal_servo.write(servo_right_W);
    }
    else if (!strcmp(variable, "rightF")) {
        horizontal_servo.write(servo_stop_W);
    }
    else if (!strcmp(variable, "right")) {
        horizontal_servo.write(servo_right_W);
        vTaskDelay(200);
        horizontal_servo.write(servo_stop_W);
    }
    else if (!strcmp(variable, "left")) {
        horizontal_servo.write(servo_left_W);
        vTaskDelay(200);
        horizontal_servo.write(servo_stop_W);
    }
```  

就是上下左右的判斷，可是又有分為長按和點擊，如果是用 `android-app.apk` 是沒有長按功能的下章有說，可是用 ` test/http_test.py ` 是可以的只要把後面網址的 <font color="orange">rightT</font> 改掉就好了

> 下面程式為 ` test/http_test.py ` 內容，其中長按 rightT 是代表往右轉，而 rightF 代表不往右轉，其他的以此類推，而點擊是後面沒加 T 或 F 的一次是按照 ` main.cpp ` 中的 servo_move_val 值改變單次點擊的旋轉大小

```py
r = requests.get("http://192.168.1.160/cmd?val=rightT")
```

## 2. `main.cpp` 中的 定義（變數 & define ...）及 初始化

### i. 定義

- 伺服馬達
  - 腳位
  - 移動參數
  - 紀錄角度

- 相機
  - 腳位
    > 詳細說明
    >> <https://github.com/espressif/esp32-camera>

- 網路
  - AP
  - /camera
  - /cmd
    > 了解更多
    >> <https://ithelp.ithome.com.tw/articles/10298661?sc=iThelpR>
    >> <https://www.lab-z.com/esp32psr/>  
    >> <https://stackoverflow.com/questions/3508338/what-is-the-boundary-in-multipart-form-data>

### ii. 初始化

 在 `setup`、`startCameraServer` 中是出初始化的程式

> # ***android***

- SW/android-app.apk 可試試看
- 還有一些 Bug
- 也是一樣接收和輸出資料# esp32-cam-PTZ
"# esp32-cam-PTZ"


# - ***使用說明*** -

-  連上 WiFi ，名字 `ESP-PTZ-1`，沒有密碼
-  如果要用 ` \SW\esp32-camera\test\http_test.py ` 傳送控制訊息，那後面的 `val` 只有以下幾種
    
    | 模式 | 右 | 左 | 上 | 下
    | :--:  | :--: | :--: | :--: | :--:
    | 開關 | right | left | top | bottom
    | 常開 | rightT | leftT
    | 常關 | rightF | leftF  

    如果要增加控制模式那就在 `cmd_handler` 中增加程式，格式如下

    ```cpp
    else if (!strcmp(variable, " !! new_command_val ")) {
      //  控制
    }
    ```

- APP 使用的時不要太激動，有 Bug 出現的時候後 APP 重開就好了，如果不行 PTZ 重開

