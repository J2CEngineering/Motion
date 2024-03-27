package com.j2cengineering.motion.controlUI

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.j2cengineering.motion.R

/**
 * TODO: document your custom view class.
 */
class CylinderListInfoView : View {

    private var _bandColor: Int = Color.BLUE
    private lateinit var _backCylinderImage:Bitmap
    private lateinit var _maskImage:Bitmap
    private lateinit var _ruleImage:Bitmap
    private lateinit var _pistonImage:Bitmap
    private lateinit var _settingBandImage:Bitmap
    private lateinit var _leftEndCapImage:Bitmap
    private lateinit var _rightEndCapImage:Bitmap
    private lateinit var _shadeImage:Bitmap
    private lateinit var _manualOverlayImage:Bitmap
    private lateinit var _calibrateOverlayImage:Bitmap
    private lateinit var _setHighMarkerImage:Bitmap
    private lateinit var _setLowMarkerImage:Bitmap
    private lateinit var _disconnectImage:Bitmap

    private lateinit var _finalImage:Bitmap
    private lateinit var _finalCanvas:Canvas

    private var _linesDrawable: Drawable? = null
    private var _disconnectDrawable:Drawable? = null

    private var _lightGrayPaint = Paint()
    private var _linearGradientPaint = Paint()
    private var _bandPaint = Paint()
    private var _shadePaint = Paint()
    private var _greyTransparentPaint = Paint()

    private var _blackPaint = Paint()

    private var _redTextPaint = Paint()

    private var _maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var _cyanPaint = Paint()
    private var _yellowPaint = Paint()

    private var _position = 0
    private var _percentage = 0
    private var _manual = false
    private var _calibrate = false
    private var _connected = false

    private var _maxSetBand = 0f
    private var _minSetBand = 0f

    private var _maxPiston = 0f
    private var _minPiston = 0f

    private var _realMaxPiston = 0f
    private var _realMinPiston = 0f

    private var _setHighPosition = -1
    private var _setLowPosition = -1

    /**
     * The band color
     */
    var bandColor: Int
        get() = _bandColor
        set(value) {
            _bandColor = value
        }

    var position: Int
        get() = _position
        set(value){
            _position = value
            this.invalidate()
        }

    var percentage: Int
        get() = _percentage
        set(value){
            _percentage = value
            this.invalidate()
        }

    var isManual: Boolean
        get() = _manual
        set(value) {
            _manual = value
            this.invalidate()
        }

    var isCalibrate: Boolean
        get() = _calibrate
        set(value) {
            _calibrate = value
            this.invalidate()
        }

    var setHighPosition: Int
        get() = _setHighPosition
        set(value){
            _setHighPosition = value
            this.invalidate()
        }

    var setLowPosition: Int
        get() = _setLowPosition
        set(value){
            _setLowPosition = value
            this.invalidate()
        }

    var connected: Boolean
        get() = _connected
        set(value){
            _connected = value
            this.invalidate()
        }

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
            attrs, R.styleable.cylinderListInfoView, defStyle, 0
        )



        _bandColor = a.getColor(
            R.styleable.cylinderListInfoView_bandColor,
            bandColor
        )

        _position = a.getInt(
            R.styleable.cylinderListInfoView_position,
            position
        )

        _percentage = a.getInt(
            R.styleable.cylinderListInfoView_percentage,
            percentage
        )

        a.recycle()

        _lightGrayPaint.style = Paint.Style.FILL
        _lightGrayPaint.color = Color.GRAY
        _lightGrayPaint.isAntiAlias = true

        _bandPaint.style = Paint.Style.FILL
        _bandPaint.isAntiAlias = true

        _maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

        _blackPaint.color = Color.BLACK
        _blackPaint.style = Paint.Style.STROKE
        _blackPaint.strokeWidth = 2F
        _blackPaint.isAntiAlias = true

        _shadePaint.color = Color.BLACK
        _shadePaint.alpha = 180
        _shadePaint.style = Paint.Style.FILL


        _redTextPaint.color = Color.RED
        _redTextPaint.style = Paint.Style.FILL
        _redTextPaint.isFakeBoldText = true
        _redTextPaint.alpha = 90

        _cyanPaint.color = resources.getColor(R.color.cyan, null)
        _cyanPaint.style = Paint.Style.FILL

        _yellowPaint.color = resources.getColor(R.color.yellow, null)
        _yellowPaint.style = Paint.Style.FILL

        _greyTransparentPaint.style = Paint.Style.FILL
        _greyTransparentPaint.color = Color.DKGRAY
        _greyTransparentPaint.isAntiAlias = true
        _greyTransparentPaint.alpha = 150

        _linesDrawable = ResourcesCompat.getDrawable(resources, R.drawable.motionappcautionlines, null)
        _disconnectDrawable = ResourcesCompat.getDrawable(resources, R.drawable.disconnectsymbol, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 200


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

        _backCylinderImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)

        var currCanvas = Canvas(_backCylinderImage)

        currCanvas.drawRect(0f, 0f,
            currCanvas.width.toFloat(), currCanvas.height / 10f, _lightGrayPaint)
        currCanvas.drawRect(0f, currCanvas.height - (currCanvas.height / 10f),
            currCanvas.width.toFloat(), currCanvas.height.toFloat(), _lightGrayPaint)

        var gradient = LinearGradient(0f, 0f, 0f, currCanvas.height - (2.0f * (currCanvas.height / 10.0f)), Color.DKGRAY, Color.LTGRAY, Shader.TileMode.CLAMP)
        _linearGradientPaint.shader = gradient

        currCanvas.drawRect(0f, currCanvas.height / 10f, currCanvas.width.toFloat(), currCanvas.height - (currCanvas.height / 10.0f), _linearGradientPaint)


        _leftEndCapImage = Bitmap.createBitmap(contentWidth / 14, contentHeight, Bitmap.Config.ARGB_8888)

        currCanvas = Canvas(_leftEndCapImage)

        _bandPaint.color = _bandColor

        gradient = LinearGradient(0f, 0f, 0f, currCanvas.height.toFloat() , Color.LTGRAY, Color.DKGRAY, Shader.TileMode.CLAMP)
        _linearGradientPaint.shader = gradient

        currCanvas.drawRect(0f, 0f, currCanvas.width - (currCanvas.width / 10f), currCanvas.height.toFloat(), _linearGradientPaint)
        currCanvas.drawRect(currCanvas.width - (currCanvas.width / 10f), 0f, currCanvas.width.toFloat(), currCanvas.height.toFloat(), _bandPaint)

        _rightEndCapImage = Bitmap.createBitmap(contentWidth / 14, contentHeight, Bitmap.Config.ARGB_8888)

        currCanvas = Canvas(_rightEndCapImage)

        currCanvas.drawRect( (currCanvas.width / 10f), 0f, currCanvas.width.toFloat() , currCanvas.height.toFloat(), _linearGradientPaint)
        currCanvas.drawRect(0f, 0f, currCanvas.width / 10f, currCanvas.height.toFloat(), _bandPaint)

        _pistonImage = Bitmap.createBitmap((contentWidth * (12f / 14f)).toInt(), contentHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_pistonImage)

        currCanvas.drawRect(0f, (currCanvas.height/ 2.0f) - (currCanvas.height / 8.0f), currCanvas.width.toFloat(),(currCanvas.height/ 2.0f) + (currCanvas.height / 8.0f), _linearGradientPaint )
        currCanvas.drawRect(0f, (currCanvas.height/ 2.0f) - (currCanvas.height / 8.0f), currCanvas.width.toFloat(),(currCanvas.height/ 2.0f) + (currCanvas.height / 8.0f), _blackPaint)

        currCanvas.drawRect(0f, currCanvas.height / 10.0f, contentWidth / 28.0f, currCanvas.height.toFloat() - (currCanvas.height / 10.0f), _linearGradientPaint)
        currCanvas.drawRect(0f, currCanvas.height / 10.0f, contentWidth / 28.0f, currCanvas.height.toFloat() - (currCanvas.height / 10.0f), _blackPaint)


        _shadeImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_shadeImage)

        currCanvas.drawRect(0f, currCanvas.height/10.0f, currCanvas.width.toFloat(), currCanvas.height.toFloat() - (currCanvas.height / 10.0f), _shadePaint)


        _ruleImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_ruleImage)

        for(i in 2..12)
        {
            currCanvas.drawLine(((currCanvas.width / 14.0f) * i), 0f, ((currCanvas.width / 14.0f) * i), currCanvas.height.toFloat(), _blackPaint)
        }


        _settingBandImage = Bitmap.createBitmap((contentWidth / 56.0f).toInt(), contentHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_settingBandImage)

        currCanvas.drawRect(0f, currCanvas.height / 10.0f, currCanvas.width / 3.0f, currCanvas.height - (currCanvas.height / 10.0f), _linearGradientPaint)
        currCanvas.drawRect(currCanvas.width * (2.0f/3.0f), currCanvas.height / 10.0f, currCanvas.width.toFloat(), currCanvas.height - (currCanvas.height / 10.0f), _linearGradientPaint)
        currCanvas.drawRect(currCanvas.width / 3.0f, currCanvas.height / 10.0f, currCanvas.width * (2f/3f), currCanvas.height - (currCanvas.height / 10.0f), _bandPaint)
        currCanvas.drawRect(0f, currCanvas.height / 10.0f, currCanvas.width.toFloat(), currCanvas.height - (currCanvas.height / 10.0f), _blackPaint)


        _maskImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)

        currCanvas = Canvas(_maskImage)
        currCanvas.drawRoundRect(0f, 0f, (contentWidth).toFloat(), (contentHeight).toFloat(), 25f , 25f, _lightGrayPaint)



        _maxSetBand = contentWidth * (2f / 14f)
        _minSetBand = contentWidth * (12f / 14f)

        _maxPiston = contentWidth * (2f / 14f)
        _minPiston = contentWidth * (12f / 14f)

        _realMaxPiston = contentWidth / 14f
        _realMinPiston = contentWidth * (13f / 14f)


        _manualOverlayImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        _redTextPaint.textSize = (_manualOverlayImage.height) * 0.70f

        val textBounds = Rect()
        val manualString = "MANUAL"

        _redTextPaint.getTextBounds(manualString, 0, manualString.length, textBounds)

        while(textBounds.width() > (_manualOverlayImage.width * 0.95))
        {
            _redTextPaint.textSize -= 0.5f
            _redTextPaint.getTextBounds(manualString, 0, manualString.length, textBounds)
        }
        currCanvas = Canvas(_manualOverlayImage)
        currCanvas.drawText(manualString, ((_manualOverlayImage.width / 2.0f) - (textBounds.width() / 2.0f)), ((_manualOverlayImage.height / 2.0f) + (textBounds.height() / 2.0f)), _redTextPaint)


        _calibrateOverlayImage = Bitmap.createBitmap(contentWidth, (contentHeight * (4.0/ 5.0)).toInt(), Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_calibrateOverlayImage)
        val linesImage = _linesDrawable
        linesImage?.alpha = 100

        val linesBitmap = linesImage!!.toBitmap(linesImage.intrinsicWidth, linesImage.intrinsicHeight, Bitmap.Config.ARGB_8888)

        currCanvas.drawBitmap(linesBitmap, Rect(0, 0, linesBitmap.width, linesBitmap.height), RectF(0f, 0f, contentWidth.toFloat(), contentHeight.toFloat()), null)



        _setHighMarkerImage = Bitmap.createBitmap(contentWidth/ 14, contentHeight, Bitmap.Config.ARGB_8888)
        _setLowMarkerImage = Bitmap.createBitmap(contentWidth/ 14, contentHeight, Bitmap.Config.ARGB_8888)


        val pointerDrawPath = Path()

        pointerDrawPath.moveTo(0.0f, 0.0f)
        pointerDrawPath.lineTo(_setHighMarkerImage.width.toFloat(), 0.0f)
        pointerDrawPath.lineTo(_setHighMarkerImage.width / 2.0f, _setHighMarkerImage.height / 10.0f)
        pointerDrawPath.lineTo(0.0f, 0.0f)

        pointerDrawPath.moveTo(0.0f, _setHighMarkerImage.height.toFloat())
        pointerDrawPath.lineTo(_setHighMarkerImage.width.toFloat(), _setHighMarkerImage.height.toFloat())
        pointerDrawPath.lineTo(_setHighMarkerImage.width / 2.0f, _setHighMarkerImage.height.toFloat() - (_setHighMarkerImage.height / 10.0f))
        pointerDrawPath.moveTo(0.0f, _setHighMarkerImage.height.toFloat())

        currCanvas = Canvas(_setHighMarkerImage)
        currCanvas.drawPath(pointerDrawPath, _cyanPaint)

        currCanvas = Canvas(_setLowMarkerImage)
        currCanvas.drawPath(pointerDrawPath, _yellowPaint)


        _disconnectImage = Bitmap.createBitmap(contentHeight, contentHeight, Bitmap.Config.ARGB_8888)
        val disconnectImg = _disconnectDrawable
        val disconnectBitmap = disconnectImg!!.toBitmap(disconnectImg.intrinsicWidth, disconnectImg.intrinsicHeight, Bitmap.Config.ARGB_8888)
        currCanvas = Canvas(_disconnectImage)
        currCanvas.drawBitmap(disconnectBitmap, Rect(0, 0, disconnectBitmap.width, disconnectBitmap.height), RectF(0f, 0f, contentHeight.toFloat(), contentHeight.toFloat()), null)


        _finalImage = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        _finalCanvas = Canvas(_finalImage)
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

        newCanvas.drawBitmap(_backCylinderImage, 0f, 0f, null)



        var pistonPos = _minPiston - ((_minPiston - _maxPiston) * (_position / 100.0f))

        if(pistonPos < _realMaxPiston)
        {
            pistonPos = _realMaxPiston
        }
        if(pistonPos > _realMinPiston)
        {
            pistonPos = _realMinPiston
        }

        newCanvas.drawBitmap(_pistonImage, pistonPos, 0f, null)

        newCanvas.drawBitmap(_shadeImage, pistonPos + _pistonImage.width / 28.0f, 0f, null)



        newCanvas.drawBitmap(_ruleImage,0f, 0f, null)


        var settingPos = _minSetBand - ((_minSetBand - _maxSetBand) * (_percentage / 100.0f))

        if(settingPos < _maxSetBand)
        {
            settingPos = _maxSetBand
        }

        if(settingPos > _minSetBand)
        {
            settingPos = _minSetBand
        }

        newCanvas.drawBitmap(_settingBandImage, (settingPos) - (_settingBandImage.width / 2.0f), 0f, null)


        if(_setHighPosition >= 0)
        {

            val highPos = _minSetBand - ((_minSetBand - _maxSetBand) * (_setHighPosition / 100.0f))

            newCanvas.drawBitmap(_setHighMarkerImage, (highPos) - (_setHighMarkerImage.width / 2.0f), 0f, null)
        }

        if(_setLowPosition >= 0)
        {

            val lowPos = _minSetBand - ((_minSetBand - _maxSetBand) * (_setLowPosition / 100.0f))

            newCanvas.drawBitmap(_setLowMarkerImage, (lowPos) - (_setLowMarkerImage.width / 2.0f), 0f, null)
        }

        if(_calibrate)
        {
            newCanvas.drawBitmap(_calibrateOverlayImage, 0f, contentHeight / 10.0f, null)
        }

        if(_manual)
        {
            newCanvas.drawBitmap(_manualOverlayImage, 0f, 0f, null)
        }

        newCanvas.drawBitmap(_leftEndCapImage, 0f, 0f, null)
        newCanvas.drawBitmap(_rightEndCapImage, (contentWidth.toFloat() - (contentWidth.toFloat() / 14.0f)), 0f, null)


        if(!_connected)
        {
            newCanvas.drawRect(0f, 0f,
                _finalImage.width.toFloat(), _finalImage.height.toFloat(), _greyTransparentPaint)

            newCanvas.drawBitmap(_disconnectImage,(contentWidth.toFloat() / 2.0f - _disconnectImage.width / 2.0f),(contentHeight.toFloat() / 2.0f - _disconnectImage.height / 2.0f), null )

        }


        newCanvas.drawBitmap(_maskImage, 0f, 0f, _maskPaint)

        canvas.drawBitmap(finalImage, paddingLeft.toFloat(), paddingTop.toFloat(), null)



    }
}