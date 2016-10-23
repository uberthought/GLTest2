precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;

void main(void)
{
    vec3 grayScaleWeights = vec3(0.3, 0.59, 0.11);
    vec3 rgb = texture2D(sTexture, vTextureCoord).rgb;
    float grayScaleValue = dot(rgb, grayScaleWeights);
    gl_FragColor.rgb = vec3(grayScaleValue, grayScaleValue, grayScaleValue);

    gl_FragColor.a = 1.0;
}

