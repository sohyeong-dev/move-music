#include <jni.h>
#include <string>

#include "opencv2/opencv.hpp"

using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_movemusic_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_movemusic_MainActivity_nativeDetectAndDisplay(JNIEnv *env, jobject thiz,
                                                               jlong addr_mat, jlong dst_mat) {
    // TODO: implement nativeDetectAndDisplay()

    Mat &src = *(Mat *) addr_mat;
    Mat &gray = *(Mat *) dst_mat;
    cvtColor(src, gray, COLOR_BGRA2GRAY);
}extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_example_movemusic_Crop_cropingJNI(JNIEnv *env, jobject thiz, jstring img_path) {
    // TODO: implement cropingJNI()

    jboolean isCopy;
    const char *imagePath = (env)->GetStringUTFChars(img_path, &isCopy);

    Mat src = imread(imagePath, IMREAD_COLOR);

    int height = src.rows;
    int width = src.cols;

    // --- inrange_hue ---

    Mat src_hsv;
    cvtColor(src, src_hsv, COLOR_BGR2HSV);

    vector<Mat> hsv_planes;
    split(src_hsv, hsv_planes);

    int half = cvRound(width / 2);

    int h_counter[256] = {0, };
    int s_counter[256] = {0, };
    int v_counter[256] = {0, };

    for (int i = 0; i < height; i++) {
        h_counter[hsv_planes[0].at<uchar>(i, half)]++;
        s_counter[hsv_planes[1].at<uchar>(i, half)]++;
        v_counter[hsv_planes[2].at<uchar>(i, half)]++;
    }

    int h_mode = 0;
    int s_mode = 0;
    int v_mode = 0;

    for (int i = 0; i < 256; i++) {
        if (h_counter[i] > h_counter[h_mode]) {
            h_mode = i;
        }
        if (s_counter[i] > s_counter[s_mode]) {
            s_mode = i;
        }
        if (v_counter[i] > v_counter[v_mode]) {
            v_mode = i;
        }
    }

    Scalar mode(h_mode, s_mode, v_mode);

    Mat mask;
    inRange(src_hsv, mode, mode, mask);

    // --- morphology ---

    Mat kernel = Mat::ones(5, 5, CV_8UC1);  // unsigned char, 1-channel
    // 팽창 후 침식: 잡음 제거
    morphologyEx(mask, mask, MORPH_CLOSE, kernel);
    // 침식 후 팽창: 앨범 이미지 내 구멍 제거
    morphologyEx(mask, mask, MORPH_OPEN, kernel);

    // --- Edge Detection ---

    Mat edge;
    Canny(mask, edge, 50, 150, 3);

    // --- Find Rectangle ---

    vector<Vec2f> lines;
    HoughLines(edge, lines, 1, CV_PI / 180, 180); // 250

    vector<int> temp_x;

    for (Vec2f li: lines) {
        float rho = li[0], theta = li[1];
        if (theta == 0.0) {
            float cos_t = cos(theta), sin_t = sin(theta);
            float x0 = rho * cos_t;
            float alpha = 1000;

            temp_x.push_back(cvRound(x0 - alpha * sin_t));
        }
    }

    sort(temp_x.begin(), temp_x.end());
    int first = temp_x.front();
    int second = temp_x.front();
    temp_x.erase(temp_x.begin());
    for (int x: temp_x) {
        if (x - first > 3) {
            second = x;
            break;
        }
    }
    int album_w = second - first;

    // --- Find Contours ---

    vector<vector<Point>> contours;
    findContours(edge, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    int cnt = 0;

    vector<Mat> mats;

    for (const auto& pts : contours) {
        Rect rc = boundingRect(pts);
        int x = rc.x;
        int y = rc.y;
        int w = rc.width;
        int h = rc.height;

        if (x > first - 3 && x < second + 3 && h > album_w - 3) {
            cnt++;
            // album image
            Mat abm = src(Range(y, y + h), Range(x, x + w));
            cvtColor(abm, abm, COLOR_BGR2RGB);
            mats.push_back(abm);
            // ocr image
            Mat roi = src(Range(y, y + h), Range(x + w, width));
            cvtColor(roi, roi, COLOR_BGR2RGB);
            mats.push_back(roi);
        }
    }

    // for ArrayList return
    jclass matClass = env->FindClass("org/opencv/core/Mat");
    jmethodID jMatCons = env->GetMethodID(matClass, "<init>", "()V");
    jmethodID getPtrMethod = env->GetMethodID(matClass, "getNativeObjAddr", "()J");
    jobjectArray matArray = env->NewObjectArray(cnt * 2, matClass, 0);

    for (int i = 0; i < mats.size(); ++i) {
        jobject jMat = env->NewObject(matClass, jMatCons);
        Mat& tempMat = *(Mat*)env->CallLongMethod(jMat, getPtrMethod);
        tempMat = mats[i];
        env->SetObjectArrayElement(matArray, i, jMat);
    }

    return matArray;
}