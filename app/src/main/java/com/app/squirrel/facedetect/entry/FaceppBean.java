package com.app.squirrel.facedetect.entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.priv.yswl.base.tool.DateJsonDeserializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * 面部识别结果的bean
 * @author chaochaowu
 */
public class FaceppBean {
    /**
     * image_id : Dd2xUw9S/7yjr0oDHHSL/Q==
     * request_id : 1470472868,dacf2ff1-ea45-4842-9c07-6e8418cea78b
     * time_used : 752
     * faces : [{"landmark":{"mouth_upper_lip_left_contour2":{"y":185,"x":146},"contour_chin":{"y":231,"x":137},"right_eye_pupil":{"y":146,"x":205},"mouth_upper_lip_bottom":{"y":195,"x":159}},"attributes":{"gender":{"value":"Female"},"age":{"value":21},"glass":{"value":"None"},"headpose":{"yaw_angle":-26.625063,"pitch_angle":12.921974,"roll_angle":22.814377},"smile":{"threshold":30.1,"value":2.566890001296997}},"face_rectangle":{"width":140,"top":89,"left":104,"height":141},"face_token":"ed319e807e039ae669a4d1af0922a0c8"}]
     */
    public static FaceppBean jsonToBean(String jsonObject) {
        if (jsonObject == null) return null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
                new DateJsonDeserializer()).create();
        Type listum = new TypeToken<FaceppBean>() {
        }.getType();
        FaceppBean result = gson.fromJson(jsonObject, listum);
        return result;
    }
    private String image_id;
    private String request_id;
    private int time_used;
    private List<FacesBean> faces;

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getTime_used() {
        return time_used;
    }

    public void setTime_used(int time_used) {
        this.time_used = time_used;
    }

    public List<FacesBean> getFaces() {
        return faces;
    }

    public void setFaces(List<FacesBean> faces) {
        this.faces = faces;
    }

    public static class FacesBean {
        /**
         * landmark : {"mouth_upper_lip_left_contour2":{"y":185,"x":146},"contour_chin":{"y":231,"x":137},"right_eye_pupil":{"y":146,"x":205},"mouth_upper_lip_bottom":{"y":195,"x":159}}
         * attributes : {"gender":{"value":"Female"},"age":{"value":21},"glass":{"value":"None"},"headpose":{"yaw_angle":-26.625063,"pitch_angle":12.921974,"roll_angle":22.814377},"smile":{"threshold":30.1,"value":2.566890001296997}}
         * face_rectangle : {"width":140,"top":89,"left":104,"height":141}
         * face_token : ed319e807e039ae669a4d1af0922a0c8
         */

        private AttributesBean attributes;
        private FaceRectangleBean face_rectangle;
        private String face_token;


        public AttributesBean getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesBean attributes) {
            this.attributes = attributes;
        }

        public FaceRectangleBean getFace_rectangle() {
            return face_rectangle;
        }

        public void setFace_rectangle(FaceRectangleBean face_rectangle) {
            this.face_rectangle = face_rectangle;
        }

        public String getFace_token() {
            return face_token;
        }

        public void setFace_token(String face_token) {
            this.face_token = face_token;
        }

        public static class AttributesBean {
            /**
             * gender : {"value":"Female"}
             * age : {"value":21}
             * glass : {"value":"None"}
             * headpose : {"yaw_angle":-26.625063,"pitch_angle":12.921974,"roll_angle":22.814377}
             * smile : {"threshold":30.1,"value":2.566890001296997}
             */

            private GenderBean gender;
            private AgeBean age;
            private FacequalityBean facequality;
            public FacequalityBean getFacequality() {
                return facequality;
            }

            public void setFacequality(FacequalityBean facequality) {
                this.facequality = facequality;
            }

            public GenderBean getGender() {
                return gender;
            }

            public void setGender(GenderBean gender) {
                this.gender = gender;
            }

            public AgeBean getAge() {
                return age;
            }

            public void setAge(AgeBean age) {
                this.age = age;
            }



            public static class GenderBean {
                /**
                 * value : Female
                 */

                private String value;

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class AgeBean {
                /**
                 * value : 21
                 */

                private int value;

                public int getValue() {
                    return value;
                }

                public void setValue(int value) {
                    this.value = value;
                }
            }

            public static class FacequalityBean {
                private float value;
                private float threshold;

                public float getValue() {
                    return value;
                }

                public void setValue(float value) {
                    this.value = value;
                }

                public float getThreshold() {
                    return threshold;
                }

                public void setThreshold(float threshold) {
                    this.threshold = threshold;
                }
            }

        }

        public static class FaceRectangleBean {
            /**
             * width : 140
             * top : 89
             * left : 104
             * height : 141
             */

            private int width;
            private int top;
            private int left;
            private int height;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getTop() {
                return top;
            }

            public void setTop(int top) {
                this.top = top;
            }

            public int getLeft() {
                return left;
            }

            public void setLeft(int left) {
                this.left = left;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }
        }
    }
}
