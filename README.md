#Introduce
    This library is imitate WeiXin Camera
#Use
    Step 1. Add the JitPack repository to your build file
        Add it in your root build.gradle at the end of repositories:
        allprojects {
            repositories {
                ...
                maven { url 'https://www.jitpack.io' }
            }
	    }
    Step 2. Add the dependency
        dependencies {
	        implementation 'com.github.LuChaoC:wxcamera:Tag'
	    }
    Step 3.take picture
        WXCamera.getInstance()
                .setType(WXCamera.PICTURE)
                .setResultCallback(new ResultCallback() {
                    @Override
                    public void onResult(String path) {
                        //path:take picture file'path
                    }

                    @Override
                    public void onError() {

                    }
                .start(Context context);
     Step 4.record video (format of mp4)
         WXCamera.getInstance()
                .setType(WXCamera.VIDEO)
                .setResultCallback(new ResultCallback() {
                    @Override
                    public void onResult(String path) {
                        //path:record video file'path
                    }

                    @Override
                    public void onError() {
                        //record video failure
                    }
                .start(Context context);
