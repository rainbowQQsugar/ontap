package io.mewa.adapterodactil.plugins;

import com.squareup.javapoet.CodeBlock;

public class ImageViewPlugin implements Plugin {
    public static final String IMAGE_VIEW = "android.widget.ImageView";

    @Override
    public String forElement() {
        return IMAGE_VIEW;
    }

    public CodeBlock process(int num, String view, Object result) {
        return CodeBlock.builder()
                .build();
    }

}
