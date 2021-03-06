package com.samsung.android.sdk.pen.pg.example2_4;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingTextInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectLine;
import com.samsung.android.sdk.pen.document.SpenObjectShape;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenFillColorEffect;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenLineColorEffect;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenLineStyleEffect;
import com.samsung.android.sdk.pen.document.textspan.SpenFontSizeSpan;
import com.samsung.android.sdk.pen.document.textspan.SpenLineSpacingParagraph;
import com.samsung.android.sdk.pen.document.textspan.SpenTextParagraphBase;
import com.samsung.android.sdk.pen.document.textspan.SpenTextSpanBase;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenLongPressListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTextChangeListener;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.pg.tool.ShapeAdapter;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingTextLayout;
import com.samsung.spensdk3.example.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PenSample2_4_ShapeLineObject extends Activity {

    private final int MODE_PEN = 0;
    private final int MODE_IMG_OBJ = 1;
    private final int MODE_TEXT_OBJ = 2;
    private final int MODE_STROKE_OBJ = 3;
    private final int MODE_SHAPE_OBJ = 4;
    private final int MODE_LINE_OBJ = 5;

    private final int SHAPE_NUM = 78;
    private final int LINE_NUM = 3;

    private final int CONTEXT_MENU_PROPERTIES_ID = 0;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private FrameLayout mSettingView;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingTextLayout mTextSettingView;

    private Dialog mShapeSelectionDialog;
    private ShapeAdapter mShapesAdapter;
    private SpenFillColorEffect mFillColorEffect;
    private SpenLineStyleEffect mLineStyleEffect;
    private SpenLineColorEffect mLineColorEffect;

    private ImageView mPenBtn;
    private ImageView mShapeObjRecogBtn;
    private ImageView mImgObjBtn;
    private ImageView mTextObjBtn;
    private ImageView mStrokeObjBtn;
    private ImageView mShapeLineObjBtn;

    private int mPreMode = MODE_PEN;
    private int mMode = MODE_PEN;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    private Rect mScreenRect;
    private int mShapeObjNumber;

    private int mObjSelectedType;
    private int mArrowBeginType;
    private int mArrowBeginSize;
    private int mArrowEndType;
    private int mArrowEndSize;

    private Dialog mShapePropertiesDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_object);
        mContext = this;

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create PenSettingView
        mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), "", spenViewLayout);

        // Create TextSettingView
        HashMap<String, String> hashMapFont = new HashMap<String, String>();
        hashMapFont.put("Droid Sans Georgian", "/system/fonts/DroidSansGeorgian.ttf");
        hashMapFont.put("Droid Serif", "/system/fonts/DroidSerif-Regular.ttf");
		hashMapFont.put("Droid Sans", "/system/fonts/DroidSans.ttf");
		hashMapFont.put("Droid Sans Mono", "/system/fonts/DroidSansMono.ttf");
        mTextSettingView = (SpenSettingTextLayout) findViewById(R.id.settingTextLayout);
        mTextSettingView.initialize("", hashMapFont, spenViewLayout);

        mSettingView = (FrameLayout) findViewById(R.id.settingView);
        mSettingView.addView(mPenSettingView);

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mTextSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);

        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // Add a Page to NoteDoc and get an instance and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        // Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initSettingInfo();
        // Register the listener
        mSpenSurfaceView.setTouchListener(mPenTouchListener);
        mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setTextChangeListener(mTextChangeListener);
        mSpenSurfaceView.setControlListener(mControlListener);
        mSpenSurfaceView.setLongPressListener(onLongPressListenner);

        // Set a button
        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mShapeObjRecogBtn = (ImageView) findViewById(R.id.recognitionBtn);
        mShapeObjRecogBtn.setOnClickListener(mShapeObjRecogBtnClickListener);

        mImgObjBtn = (ImageView) findViewById(R.id.imgObjBtn);
        mImgObjBtn.setOnClickListener(mImgObjBtnClickListener);

        mTextObjBtn = (ImageView) findViewById(R.id.textObjBtn);
        mTextObjBtn.setOnClickListener(mTextObjBtnClickListener);

        mStrokeObjBtn = (ImageView) findViewById(R.id.strokeObjBtn);
        mStrokeObjBtn.setOnClickListener(mStrokeObjBtnClickListener);

        mShapeLineObjBtn = (ImageView) findViewById(R.id.choiceBtn);
        mShapeLineObjBtn.setOnClickListener(mShapeLineObjBtnClickListener);

        selectButton(mPenBtn);
        initShapeSelectionDialog();

        if (isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mToolType = SpenSurfaceView.TOOL_SPEN;
        }
        mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
    }

    private void initSettingInfo() {
        // Initialize Pen settings
        List<SpenPenInfo> penList = new ArrayList<SpenPenInfo>();
        SpenPenManager penManager = new SpenPenManager(mContext);
        penList = penManager.getPenInfoList();
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        for (SpenPenInfo info : penList) {
            if (info.name.equalsIgnoreCase("Brush")) {
                penInfo.name = info.className;
                break;
            }
        }
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);

        // Initialize text settings
        SpenSettingTextInfo textInfo = new SpenSettingTextInfo();
        int mCanvasWidth = mScreenRect.width();

        if (mSpenSurfaceView != null) {
            if (mSpenSurfaceView.getCanvasWidth() < mSpenSurfaceView.getCanvasHeight()) {
                mCanvasWidth = mSpenSurfaceView.getCanvasWidth();
            } else {
                mCanvasWidth = mSpenSurfaceView.getCanvasHeight();
            }
            if (mCanvasWidth == 0) {
                mCanvasWidth = mScreenRect.width();
            }
        }
//        textInfo.size = Math.round(18 * mCanvasWidth / 360);
        mSpenSurfaceView.setTextSettingInfo(textInfo);
        mTextSettingView.setInfo(textInfo);
    }

    private SpenLongPressListener onLongPressListenner = new SpenLongPressListener(){
        @Override
        public void onLongPressed(MotionEvent event){
            enableButton(true);
        }
    };

    private final SpenTouchListener mPenTouchListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getToolType(0) == mToolType) {
                // Check if the control is created.
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control == null) {
                    // When Pen touches the display while it is in Add ObjectImage mode
                    if (mMode == MODE_IMG_OBJ) {
                        // Set a bitmap file to ObjectImage.
                        SpenObjectImage imgObj = new SpenObjectImage();
                        Bitmap imageBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                R.drawable.ic_launcher);
                        imgObj.setImage(imageBitmap);

                        // Set the location to insert ObjectImage and add it to PageDoc.
                        PointF canvasPos = getCanvasPoint(event);
                        RectF rect = new RectF(canvasPos.x - (imageBitmap.getWidth() / 2), canvasPos.y
                                - (imageBitmap.getHeight() / 2), canvasPos.x + (imageBitmap.getWidth() / 2),
                                canvasPos.y + (imageBitmap.getHeight() / 2));
                        imgObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(imgObj);
                        mSpenSurfaceView.update();

                        imageBitmap.recycle();
                        return true;
                        // When Pen touches the display while it is in Add ObjectTextBox mode
                    } else if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT) {
                        // Set the location to insert ObjectTextBox and add it to PageDoc.
                        SpenObjectTextBox textObj = new SpenObjectTextBox();
                        PointF canvasPos = getCanvasPoint(event);
                        float x = canvasPos.x;
                        float y = canvasPos.y;
                        float textBoxHeight = getTextBoxDefaultHeight(textObj);
                        if ((y + textBoxHeight) > mSpenPageDoc.getHeight()) {
                            y = mSpenPageDoc.getHeight() - textBoxHeight;
                        }
                        RectF rect = new RectF(x, y, x + 350, y + textBoxHeight);
                        textObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(textObj);
                        mSpenPageDoc.selectObject(textObj);
                        mSpenSurfaceView.update();
                        // When Pen touches the display while it is in Add ObjectStroke mode
                    } else if (mMode == MODE_STROKE_OBJ) {
                        // Set the location to insert ObjectStroke and add it to PageDoc.
                        PointF canvasPos = getCanvasPoint(event);
                        float posX = canvasPos.x;
                        int pointSize = 157;

                        PointF[] points = new PointF[pointSize];
                        float[] pressures = new float[pointSize];
                        int[] timestamps = new int[pointSize];

                        for (int i = 0; i < pointSize; i++) {
                            points[i] = new PointF();
                            points[i].x = posX++;
                            points[i].y = (float) (canvasPos.y + Math.sin(.04 * i) * 50);
                            pressures[i] = 1;
                            timestamps[i] = (int) android.os.SystemClock.uptimeMillis();
                        }

                        SpenObjectStroke strokeObj = new SpenObjectStroke(mPenSettingView.getInfo().name, points,
                                pressures, timestamps);
                        strokeObj.setPenSize(mPenSettingView.getInfo().size);
                        strokeObj.setColor(mPenSettingView.getInfo().color);
                        mSpenPageDoc.appendObject(strokeObj);
                        mSpenSurfaceView.update();
                        // When Pen touches the display while it is in Add ObjectShape mode
                    } else if (mMode == MODE_SHAPE_OBJ) {

                        SpenObjectShape shapeObj = null;
                        try {
                            shapeObj = new SpenObjectShape(mShapeObjNumber);
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Not supported shape type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }

                        PointF canvasPos = getCanvasPoint(event);
                        RectF rect = new RectF(canvasPos.x - 150, canvasPos.y - 150, canvasPos.x + 150,
                                canvasPos.y + 150);
                        shapeObj.setRect(rect, false);

                        SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                        lineStyle.setWidth(4);
                        shapeObj.setLineStyleEffect(lineStyle);

                        mSpenPageDoc.appendObject(shapeObj);
                        mSpenSurfaceView.update();

                    } else if (mMode == MODE_LINE_OBJ) {
                        SpenObjectLine line = null;

                        try {
                            PointF canvasPos = getCanvasPoint(event);
                            RectF rect = new RectF(canvasPos.x - 200, canvasPos.y - 200, canvasPos.x + 200,
                                    canvasPos.y + 200);
                            line = new SpenObjectLine(mShapeObjNumber, new PointF(rect.left, rect.top), new PointF(
                                    rect.right, rect.bottom));
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Not supported line type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }

                        SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                        lineStyle.setWidth(4);
                        line.setLineStyleEffect(lineStyle);

                        mSpenPageDoc.appendObject(line);
                        mSpenSurfaceView.update();
                    }
                }
            }
            return false;
        }
    };

    private float getTextBoxDefaultHeight(SpenObjectTextBox textBox) {
        if (textBox == null) {
            return 0;
        }

        float height = 0, lineSpacing = 0, lineSpacePercent = 1.3f;
        float margin = textBox.getTopMargin() + textBox.getBottomMargin();

        ArrayList<SpenTextParagraphBase> pInfo = textBox.getTextParagraph();
        if (pInfo != null) {
            for (SpenTextParagraphBase info : pInfo) {
                if (info instanceof SpenLineSpacingParagraph) {
                    if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PERCENT) {
                        lineSpacePercent = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    } else if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PIXEL) {
                        lineSpacing = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    }
                }
            }
        }

        if (lineSpacing != 0){
            height = lineSpacing + margin;
        } else {
            float fontSize = mSpenPageDoc.getWidth()/20;
            ArrayList<SpenTextSpanBase> sInfo =
                    textBox.findTextSpan(textBox.getCursorPosition(), textBox.getCursorPosition());
            if (sInfo != null) {
                for (SpenTextSpanBase info : sInfo) {
                    if (info instanceof SpenFontSizeSpan) {
                        fontSize = ((SpenFontSizeSpan) info).getSize();
                        break;
                    }
                }
            }
            height = fontSize * lineSpacePercent;
        }

        return height;
    }

    private PointF getCanvasPoint(MotionEvent event) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        return new PointF(event.getX() / zoom + panX, event.getY() / zoom + panY);
    }

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            // When Spen is in stroke (pen) mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_STROKE) {
                // If PenSettingView is open, close it.
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                    // If PenSettingView is not open, open it.
                } else {
                    mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                    mPenSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in stroke (pen) mode, change it to stroke mode.
            } else {
                mMode = MODE_PEN;
                selectButton(mPenBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
            }
        }
    };

    private final OnClickListener mShapeObjRecogBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mMode = MODE_PEN;
            selectButton(mShapeObjRecogBtn);
            mSpenSurfaceView.closeControl();
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_RECOGNITION);

        }
    };

    private final OnClickListener mImgObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            mMode = MODE_IMG_OBJ;
            selectButton(mImgObjBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
        }
    };

    private final OnClickListener mTextObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();
            closeSettingView();
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_TEXT);
            mMode = MODE_TEXT_OBJ;
            selectButton(mTextObjBtn);
        }
    };

    private final OnClickListener mStrokeObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            mMode = MODE_STROKE_OBJ;
            selectButton(mStrokeObjBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                if (mMode == MODE_PEN) {
                    SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                    penInfo.color = color;
                    mPenSettingView.setInfo(penInfo);
                } else if (mMode == MODE_TEXT_OBJ || mMode == MODE_SHAPE_OBJ) {
                    SpenSettingTextInfo textInfo = mSpenSurfaceView.getTextSettingInfo();
                    textInfo.color = color;
                    mTextSettingView.setInfo(textInfo);
                }
            }
        }
    };

    SpenTextChangeListener mTextChangeListener = new SpenTextChangeListener() {

        @Override
        public boolean onSelectionChanged(int arg0, int arg1) {
            return false;
        }

        @Override
        public void onMoreButtonDown(SpenObjectTextBox arg0) {
        }

        @Override
        public void onChanged(SpenSettingTextInfo info, int state) {
            if (mTextSettingView != null) {
                if (state == CONTROL_STATE_SELECTED) {
                    mTextSettingView.setInfo(info);
                }
            }
        }

        @Override
        public void onFocusChanged(boolean gainFocus) {
            if (mTextSettingView != null) {
                if (gainFocus == true) {
                    // show text setting
                    mTextSettingView.setVisibility(View.VISIBLE);
                    mPreMode = mMode;
                    mMode = MODE_TEXT_OBJ;
                } else {
                    // hide text setting
                    mTextSettingView.setVisibility(View.GONE);
                    mMode = mPreMode;
                }
            }
        }
    };

    private final SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public void onRotationChanged(float arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onRectChanged(RectF arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> arg0) {
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> objectList, int itemId) {
            switch (itemId) {
            // Properties of object shape/line
            case CONTEXT_MENU_PROPERTIES_ID:
                shapeProperties();
                mSpenSurfaceView.closeControl();
                break;
            default:
                break;
            }

            return true;
        }

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList, ArrayList<Rect> relativeRectList,
                ArrayList<SpenContextMenuItemInfo> menu, ArrayList<Integer> styleList, int pressType, PointF point) {
            // Set the Context menu
            SpenObjectBase object = objectList.get(0);
            if (object.getType() == SpenObjectBase.TYPE_SHAPE || object.getType() == SpenObjectBase.TYPE_LINE) {
                menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_PROPERTIES_ID, "Properties", true));
            }

            return true;
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> arg0) {
            return false;
        }
    };

    private SpenTouchListener onPreTouchSurfaceViewListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    enableButton(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    enableButton(true);
                    break;
            }
            return false;
        }
    };

    private final OnClickListener mShapeLineObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectButton(mShapeLineObjBtn);

            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);

            mSpenSurfaceView.closeControl();
            mShapeSelectionDialog.show();
            closeSettingView();
        }
    };

    private void initShapeSelectionDialog() {
        ArrayList<Integer> shapes = new ArrayList<Integer>();
        for (int i = 0; i < SHAPE_NUM + LINE_NUM; i++) {
            shapes.add(i);
        }
        mShapesAdapter = new ShapeAdapter(mContext, shapes);

        mShapeSelectionDialog = new Dialog(mContext);
        mShapeSelectionDialog.setContentView(R.layout.dialog_shapes);
        mShapeSelectionDialog.setTitle("Shape/Line");

        GridView gridShapes = (GridView) mShapeSelectionDialog.findViewById(R.id.gridShapes);
        gridShapes.setAdapter(mShapesAdapter);
        gridShapes.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < SHAPE_NUM) {
                    mMode = MODE_SHAPE_OBJ;
                    mShapeObjNumber = position + 1;
                    SpenObjectShape shape = null;

                    try {
                        shape = new SpenObjectShape(mShapeObjNumber);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Not supported shape type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    shape.setRect(new RectF(100, 100, 400, 400), false);

                    SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                    lineStyle.setWidth(4);
                    shape.setLineStyleEffect(lineStyle);

                    mSpenPageDoc.appendObject(shape);
                    mSpenSurfaceView.update();
                    mShapeSelectionDialog.dismiss();
                } else {
                    mMode = MODE_LINE_OBJ;
                    SpenObjectLine line = null;
                    mShapeObjNumber = position - SHAPE_NUM;

                    try {
                        line = new SpenObjectLine(mShapeObjNumber, new PointF(100, 100), new PointF(500, 500));
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Not supported line type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                    lineStyle.setWidth(4);
                    line.setLineStyleEffect(lineStyle);

                    mSpenPageDoc.appendObject(line);
                    mSpenSurfaceView.update();
                    mShapeSelectionDialog.dismiss();
                }
            }
        });
    }

    private void shapeProperties() {
        final SpenObjectBase object = mSpenPageDoc.getSelectedObject().get(0);
        mObjSelectedType = object.getType();

        mLineStyleEffect = new SpenLineStyleEffect();
        mLineColorEffect = new SpenLineColorEffect();

        if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
            ((SpenObjectShape) object).getLineStyleEffect(mLineStyleEffect);
            ((SpenObjectShape) object).getLineColorEffect(mLineColorEffect);
            mFillColorEffect = new SpenFillColorEffect();
            ((SpenObjectShape) object).getFillEffect(mFillColorEffect);
        } else if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
            ((SpenObjectLine) object).getLineStyleEffect(mLineStyleEffect);
            ((SpenObjectLine) object).getLineColorEffect(mLineColorEffect);
        }

        mShapePropertiesDialog = new Dialog(mContext);
        mShapePropertiesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mShapePropertiesDialog.setContentView(R.layout.dialog_shapes_properties);
        updatePropertiesDiablogLayout();

        if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
            String arrayColor[] = new String[6];
            arrayColor[0] = "No Fill";
            arrayColor[1] = "Black";
            arrayColor[2] = "Blue";
            arrayColor[3] = "Red";
            arrayColor[4] = "Yellow";
            arrayColor[5] = "Green";

            final ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(0);
            colors.add(Color.BLACK);
            colors.add(Color.BLUE);
            colors.add(Color.RED);
            colors.add(Color.YELLOW);
            colors.add(Color.GREEN);

            LinearLayout fillColorView = (LinearLayout) mShapePropertiesDialog.findViewById(R.id.fillColorView);
            fillColorView.setVisibility(View.VISIBLE);

            Spinner fillColorSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spFillColor);
            ArrayAdapter<String> adapterFillColor = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayColor);
            fillColorSpinner.setAdapter(adapterFillColor);
            fillColorSpinner.setSelection(colors.indexOf(mFillColorEffect.getSolidColor()));

            fillColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mFillColorEffect.setSolidColor(colors.get(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        }

        String arrayColor[] = new String[5];
        arrayColor[0] = "Black";
        arrayColor[1] = "Blue";
        arrayColor[2] = "Red";
        arrayColor[3] = "Yellow";
        arrayColor[4] = "Green";

        final ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);

        // SPinner Line Color
        Spinner lineColorSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spLineColor);
        ArrayAdapter<String> adapterLineColor = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayColor);
        lineColorSpinner.setAdapter(adapterLineColor);
        lineColorSpinner.setSelection(colors.indexOf(mLineColorEffect.getSolidColor()));
        lineColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                mLineColorEffect.setSolidColor(colors.get(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final EditText tbWidthInput = (EditText) mShapePropertiesDialog.findViewById(R.id.tbWidth);
        tbWidthInput.setText("" + mLineStyleEffect.getWidth());

        // SPinner Compound type
        final Spinner compTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spCompType);
        final String arrayCompType[] = new String[5];
        arrayCompType[0] = "Simple";
        arrayCompType[1] = "Double";
        arrayCompType[2] = "Thin";
        arrayCompType[3] = "Thick";
        arrayCompType[4] = "Triple";

        ArrayAdapter<String> adapterCompTyper = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayCompType);
        compTypeSpinner.setAdapter(adapterCompTyper);
        compTypeSpinner.setSelection(mLineStyleEffect.getCompoundType());
        compTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                try {
                    mLineStyleEffect.setCompoundType(index);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(mContext, "Not supported compound type: \"" + arrayCompType[index] + "\"",
                            Toast.LENGTH_SHORT).show();
                    compTypeSpinner.setSelection(mLineStyleEffect.getCompoundType());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // SPinner Dash type
        Spinner dashTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spDashType);
        String arrayDashType[] = new String[8];
        arrayDashType[0] = "Solid";
        arrayDashType[1] = "Round Dot";
        arrayDashType[2] = "Square Dot";
        arrayDashType[3] = "Dash";
        arrayDashType[4] = "Dash Dot";
        arrayDashType[5] = "Long Dash";
        arrayDashType[6] = "Long Dash Dot";
        arrayDashType[7] = "Long Dash Dot Dot";

        ArrayAdapter<String> adapterDashTyper = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayDashType);
        dashTypeSpinner.setAdapter(adapterDashTyper);
        dashTypeSpinner.setSelection(mLineStyleEffect.getDashType());
        dashTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                mLineStyleEffect.setDashType(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
            // Enable arrow setting view
            LinearLayout arrowSettingView = (LinearLayout) mShapePropertiesDialog.findViewById(R.id.arrowSetting);
            arrowSettingView.setVisibility(View.VISIBLE);

            // SPinner Arrow Begin Type
            mArrowBeginType = mLineStyleEffect.getBeginArrowType();
            mArrowBeginSize = mLineStyleEffect.getBeginArrowSize();
            mArrowEndType = mLineStyleEffect.getEndArrowType();
            mArrowEndSize = mLineStyleEffect.getEndArrowSize();

            String arrayArrowType[] = new String[6];
            arrayArrowType[0] = "None";
            arrayArrowType[1] = "Arrow";
            arrayArrowType[2] = "Open Arrow";
            arrayArrowType[3] = "Stealth Arrow";
            arrayArrowType[4] = "Diamond Arrow";
            arrayArrowType[5] = "Oval Arrow";

            String arrayArrowSize[] = new String[3];
            arrayArrowSize[0] = "Normal";
            arrayArrowSize[1] = "Small";
            arrayArrowSize[2] = "Big";

            Spinner arrowBTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowBeginType);
            ArrayAdapter<String> adapterArrowBType = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowType);

            arrowBTypeSpinner.setAdapter(adapterArrowBType);
            arrowBTypeSpinner.setSelection(mArrowBeginType);
            arrowBTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowBeginType = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow Begin Size
            Spinner arrowBSizeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowBeginSize);
            ArrayAdapter<String> adapterArrowBSize = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowSize);

            arrowBSizeSpinner.setAdapter(adapterArrowBSize);
            arrowBSizeSpinner.setSelection(mArrowBeginSize);
            arrowBSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowBeginSize = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow End Type
            Spinner arrowETypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowEndType);
            ArrayAdapter<String> adapterArrowEType = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowType);

            arrowETypeSpinner.setAdapter(adapterArrowEType);
            arrowETypeSpinner.setSelection(mArrowEndType);
            arrowETypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowEndType = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow End Size
            Spinner arrowESizeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowEndSize);
            ArrayAdapter<String> adapterArrowESize = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowSize);

            arrowESizeSpinner.setAdapter(adapterArrowESize);
            arrowESizeSpinner.setSelection(mArrowEndSize);
            arrowESizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowEndSize = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        }

        Button btnOK = (Button) mShapePropertiesDialog.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
                    // Set Width
                    if (tbWidthInput.getText().length() == 0) {
                        tbWidthInput.setError("Please input value");
                        return;
                    }
                    float width = Float.parseFloat(tbWidthInput.getText().toString());
                    mLineStyleEffect.setWidth(width);

                    // Set fill color effect
                    ((SpenObjectShape) object).setFillEffect(mFillColorEffect);

                    ((SpenObjectShape) object).setLineStyleEffect(mLineStyleEffect);
                    ((SpenObjectShape) object).setLineColorEffect(mLineColorEffect);
                } else if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
                    // Set Width
                    if (tbWidthInput.getText().length() == 0) {
                        tbWidthInput.setError("Please input value");
                        return;
                    }
                    float width = Float.parseFloat(tbWidthInput.getText().toString());
                    mLineStyleEffect.setWidth(width);

                    mLineStyleEffect.setBeginArrow(mArrowBeginType, mArrowBeginSize);
                    mLineStyleEffect.setEndArrow(mArrowEndType, mArrowEndSize);

                    ((SpenObjectLine) object).setLineStyleEffect(mLineStyleEffect);
                    ((SpenObjectLine) object).setLineColorEffect(mLineColorEffect);
                }

                mSpenSurfaceView.update();
                mShapePropertiesDialog.dismiss();
            }
        });

        Button btnCancel = (Button) mShapePropertiesDialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mShapePropertiesDialog.dismiss();
            }
        });

        mShapePropertiesDialog.show();
        return;
    }

    private void enableButton(boolean isEnable) {
        mPenBtn.setEnabled(isEnable);
        mImgObjBtn.setEnabled(isEnable);
        mTextObjBtn.setEnabled(isEnable);
        mStrokeObjBtn.setEnabled(isEnable);
        mShapeLineObjBtn.setEnabled(isEnable);
        mShapeObjRecogBtn.setEnabled(isEnable);
    }

    private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        mPenBtn.setSelected(false);
        mShapeObjRecogBtn.setSelected(false);
        mImgObjBtn.setSelected(false);
        mTextObjBtn.setSelected(false);
        mStrokeObjBtn.setSelected(false);
        mShapeLineObjBtn.setSelected(false);

        v.setSelected(true);

        closeSettingView();
    }

    private void closeSettingView() {
        // Close all the setting views.
        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
        mTextSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    private void updatePropertiesDiablogLayout() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getWindowManager().getDefaultDisplay().getRectSize(mScreenRect);

        RelativeLayout layout = (RelativeLayout) mShapePropertiesDialog.findViewById(R.id.shapePropertiesLayout);
        LayoutParams params = layout.getLayoutParams();

        params.height = (int) (335 * (metrics.densityDpi / 160f));
        if (400 * (metrics.densityDpi / 160f) > mScreenRect.height()) {
            params.height = mScreenRect.height() - 200;
            layout.setLayoutParams(params);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mSpenSurfaceView.cancelStroke();

        if (mShapePropertiesDialog != null && mShapePropertiesDialog.isShowing()) {
            updatePropertiesDiablogLayout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mTextSettingView != null) {
            mTextSettingView.close();
        }

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.closeControl();
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    };
}