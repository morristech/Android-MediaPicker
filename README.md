# Media Picker
Awesome Image Picker library will pick images/gifs with beautiful interface. Supports image or gif, Single and Multiple Image selection.

 ![Media Picker - Example1](https://s19.postimg.org/4bxmouwbn/Image_Picker_example_1.png)
 `` ``
 ![Media Picker - Example2](https://s19.postimg.org/jlxhw1rtv/Image_Picker_example_2.png)
 `` ``
 ![Media Picker - Example3](https://s19.postimg.org/4ehibozz7/Image_Picker_example_3.png)
 `` ``
 ![Media Picker - Example4](https://s19.postimg.org/91nkdgnc3/Image_Picker_example_4.png)
 
  
#### Kindly use the following links to use this library:

In build.gradle (Project)
```java
allprojects {
  repositories {
			...
		maven { url "https://jitpack.io" }
	}
}
```
And then in the other gradle file(may be your app gradle or your own module library gradle, but never add in both of them to avoid conflict.)
```java
dependencies {
    compile 'com.github.myinnos:mediapicker:1.0.0'
}
```
How to use
-----
**Step 1:** start intent to open awesome image picker gallery:
```java
Intent intent = new Intent(this, AlbumSelectActivity.class);
intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, <LIMIT>); // set limit for image selection
startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
```
**Step 2:** onActivityResult : [#Example](https://github.com/myinnos/AwesomeImagePicker/blob/master/app/src/main/java/in/myinnos/imagepicker/MainActivity.java "Example")
```java
@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ConstantsCustomGallery.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

            for (int i = 0; i < images.size(); i++) {
                Uri uri = Uri.fromFile(new File(images.get(i).path));
                // start play with image uri
                ..........
            }
        }
    }
```
**IMP Note:** Require STORAGE_PERMISSIONS if Build.VERSION.SDK_INT >= 23.
##### Any Queries? or Feedback, please let me know by opening a [new issue](https://github.com/morristech/Android-MediaPicker/issues/new)!

## Contact
#### Wade Morris
* :email: e-mail: wade @ morristech . co . za

License
-------

    Copyright 2018 Wade Morris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
