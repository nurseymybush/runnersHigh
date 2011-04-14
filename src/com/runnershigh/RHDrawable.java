package com.runnershigh;

import android.content.Context;
import android.graphics.Rect;

public class RHDrawable extends Mesh {
	private int width;
	private int height;
	
	public RHDrawable(Context context, OpenGLRenderer glrenderer, int _x, int _y, int _z, int _width, int _height) {
		x = _x;
		y = _y;
		z = _z;
		
		width= _width;
		height= _height;
		
		float textureCoordinates[] = { 0.0f, 1.0f, //
				1.0f, 1.0f, //
				0.0f, 0.0f, //
				1.0f, 0.0f, //
		};

		short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };

		float[] vertices = new float[] { 0, 0, 0, width, 0, 0.0f, 0, height,
				0.0f, width, height, 0.0f };

		setIndices(indices);
		setVertices(vertices);
		setTextureCoordinates(textureCoordinates);
	}
	public Rect getRect() {
		return new Rect((int)x,(int)y+height,(int)x+width,(int)y);
	}
}