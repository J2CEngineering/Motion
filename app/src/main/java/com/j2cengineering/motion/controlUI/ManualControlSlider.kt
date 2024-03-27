package com.j2cengineering.motion.controlUI

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.j2cengineering.motion.R
import kotlin.math.roundToInt

/**
 * TODO: document your custom view class.
 */
class ManualControlSlider : View {

    private lateinit var _leftcap:Bitmap
    private lateinit var _rightcap:Bitmap
    private lateinit var _background:Bitmap
    private lateinit var _ruleImage:Bitmap

    private lateinit var _sliderButton:Bitmap
    private lateinit var _sliderButtonInactive:Bitmap


    private lateinit var _finalImage:Bitmap
    private lateinit var _finalCanvas:Canvas

    private var _leftCapDrawable:Drawable? = null
    private var _rightCapDrawable:Drawable? = null
    private var _sliderButtonDrawable:Drawable? = null
    private var _sliderButtonInactiveDrawable:Drawable? = null

    private var _lightGrayPaint = Paint()
    private var _blackPaint = Paint()

    private var _pointerPathMin = 0.0f
    private var _pointerPathMax = 0.0f

    private var _pointerPathCurr = 0.0f


    private var _manualControl = false
    private var _percent = 50

    private var listener: PercentChangeListener? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ManualControlSlider, defStyle, 0
        )

        a.recycle()

        _lightGrayPaint.style = Paint.Style.FILL
        _lightGrayPaint.color = Color.GRAY
        _lightGrayPaint.isAntiAlias = true

        _blackPaint.color = Color.BLACK
        _blackPaint.style = Paint.Style.STROKE
        _blackPaint.strokeWidth = 2F
        _blackPaint.isAntiAlias = true




        _leftCapDrawable = ResourcesCompat.getDrawable(resources, R.drawable.leftslidercap, null)
        _rightCapDrawable = ResourcesCompat.getDrawable(resources, R.drawable.rightslidercap, null)
        _sliderButtonDrawable = ResourcesCompat.getDrawable(resources, R.drawable.sliderbutton, null)
        _sliderButtonInactiveDrawable = ResourcesCompat.getDrawable(resources, R.drawable.sliderbuttoninactive, null)
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    fun getManualControl(): Boolean{
        return _manualControl
    }

    fun setManualControl(newControl:Boolean, currPercent:Int)
    {
        _manualControl = newControl

        if(_manualControl)
        {
            _percent = currPercent
        }
        else
        {
            _percent = 50
        }

        _pointerPathCurr = _pointerPathMax - ((_pointerPathMax - _pointerPathMin) * (_percent / 100.0f))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 50


        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        var height: Int


        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);



    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setupBitmaps()
    }

    private fun setupBitmaps()
    {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        _finalImage = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)

        _background = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        var currCanvas = Canvas(_background)

        currCanvas.drawRect(0f, 0f, _background.width.toFloat(), _background.height.toFloat(), _lightGrayPaint)


        _ruleImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_ruleImage)

        val rulerSize = contentWidth - (contentHeight)
        val rulerChunk = rulerSize.toFloat() / 10.0f

        for(i in 0..10)
        {
            currCanvas.drawLine((contentHeight.toFloat() / 2.0f) + rulerChunk * i, 0f, (contentHeight.toFloat() / 2.0f) + rulerChunk * i, contentHeight.toFloat(), _blackPaint)
        }

        _leftcap = Bitmap.createBitmap(contentHeight / 2, contentHeight, Bitmap.Config.ARGB_8888)
        val leftCapBitmap = _leftCapDrawable!!.toBitmap(_leftCapDrawable!!.intrinsicWidth, _leftCapDrawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_leftcap)
        currCanvas.drawBitmap(leftCapBitmap, Rect(0, 0, leftCapBitmap.width, leftCapBitmap.height), RectF(0f, 0f, _leftcap.width.toFloat(), _leftcap.height.toFloat()), null)

        _rightcap = Bitmap.createBitmap(contentHeight / 2, contentHeight, Bitmap.Config.ARGB_8888)
        val rightCapBitmap = _rightCapDrawable!!.toBitmap(_rightCapDrawable!!.intrinsicWidth, _rightCapDrawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_rightcap)
        currCanvas.drawBitmap(rightCapBitmap, Rect(0, 0, rightCapBitmap.width, rightCapBitmap.height), RectF(0f, 0f, _rightcap.width.toFloat(), _rightcap.height.toFloat()), null)


        _sliderButton = Bitmap.createBitmap(contentHeight, contentHeight, Bitmap.Config.ARGB_8888)
        val sliderButtonBitmap = _sliderButtonDrawable!!.toBitmap(_sliderButtonDrawable!!.intrinsicWidth, _sliderButtonDrawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_sliderButton)
        currCanvas.drawBitmap(sliderButtonBitmap, Rect(0, 0, sliderButtonBitmap.width, sliderButtonBitmap.height), RectF(0f, 0f, _sliderButton.width.toFloat(), _sliderButton.height.toFloat()), null)

        _sliderButtonInactive = Bitmap.createBitmap(contentHeight, contentHeight, Bitmap.Config.ARGB_8888)
        val sliderButtonInactiveBitmap = _sliderButtonInactiveDrawable!!.toBitmap(_sliderButtonInactiveDrawable!!.intrinsicWidth, _sliderButtonInactiveDrawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_sliderButtonInactive)
        currCanvas.drawBitmap(sliderButtonInactiveBitmap, Rect(0, 0, sliderButtonInactiveBitmap.width, sliderButtonInactiveBitmap.height), RectF(0f, 0f, _sliderButtonInactive.width.toFloat(), _sliderButtonInactive.height.toFloat()), null)

        _finalImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        _finalCanvas = Canvas(_finalImage)

        _pointerPathMax = contentWidth.toFloat() - contentHeight.toFloat()
        _pointerPathMin = 0.0f

        _pointerPathCurr = _pointerPathMax - ((_pointerPathMax - _pointerPathMin) * (_percent / 100.0f))

    }

    private fun invalidateTextPaintAndMeasurements() {
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener(){



        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            if(distanceX == 0.0f)
                return  false

            _pointerPathCurr -= distanceX

            if(_pointerPathCurr > _pointerPathMax)
            {
                _pointerPathCurr =  _pointerPathMax
            }

            if(_pointerPathCurr < _pointerPathMin)
            {
                _pointerPathCurr = _pointerPathMin
            }

            _percent = 100 - (((_pointerPathCurr - _pointerPathMin) / (_pointerPathMax - _pointerPathMin)) * 100.0).roundToInt()

            if(listener != null) {
                listener?.onPercentChanged(_percent)
            }
            invalidate()

            return true
        }
    }

    private val detector: GestureDetector = GestureDetector(context, gestureListener)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event != null) {
            detector.onTouchEvent(event).let{result ->
                if(!result){

                    when(event.action) {
                        MotionEvent.ACTION_UP-> {
                            Log.i("Interface Testing", "Button Up")
                            if(listener != null) {
                                listener?.onPercentChanged(this._percent)
                            }
                            true
                        }

                        MotionEvent.ACTION_DOWN-> {
                            if((event.x > _pointerPathCurr) &&
                                (event.x < (_pointerPathCurr + _sliderButton.width)) &&
                                (event.y > 0.0) &&
                                (event.y < (_sliderButton.height)) &&
                                this._manualControl) {
                                this.parent.requestDisallowInterceptTouchEvent(true)
                                Log.i("Interface Testing", "Button Down")
                                true
                            }
                            else
                            {
                                false
                            }
                        }

                        else-> false

                    }

                }else true
            }
        } else {
            false
        }
    }

    public interface PercentChangeListener{
        public fun onPercentChanged(percent: Int)
    }

    public fun setPercentChangeListener(listener: PercentChangeListener)
    {
        this.listener = listener
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val finalImage = _finalImage

        val newCanvas = _finalCanvas

        newCanvas.drawBitmap(_background, 0f, 0f, null)

        newCanvas.drawBitmap(_leftcap, 0f, 0f, null)
        newCanvas.drawBitmap(_rightcap, contentWidth.toFloat() - (_rightcap.width.toFloat()), 0f, null )


        if(_manualControl) {
            newCanvas.drawBitmap(_sliderButton, _pointerPathCurr, 0f, null)
        }
        else {
            newCanvas.drawBitmap(_sliderButtonInactive, _pointerPathCurr, 0f, null)
        }

        newCanvas.drawBitmap(_ruleImage, paddingLeft.toFloat(), 0f, null)
        canvas.drawBitmap(finalImage, paddingLeft.toFloat(), paddingTop.toFloat(), null)
    }
}