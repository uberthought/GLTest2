attribute vec4 aTextureCoord;
attribute vec4 aPosition;
varying vec2 vTextureCoord;
uniform mat4 modelViewMat;


void main() {
//	gl_Position = aPosition;
	gl_Position = modelViewMat * aPosition;
	vTextureCoord = aTextureCoord.xy;
}
