package com.tarmedtar.screendoodle;

import android.app.Service;
import android.content.Intent;
import android.graphics.*;
import android.os.IBinder;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class DrawingOverlayService extends Service {
    private WindowManager wm;
    private DrawView drawing;
    private LinearLayout tools;
    private WindowManager.LayoutParams drawingParams;
    private int color = Color.RED;
    private float strokeWidth = 8f;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        drawing = new DrawView();
        drawingParams = new WindowManager.LayoutParams(
            -1, -1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        drawingParams.gravity = Gravity.TOP | Gravity.START;
        wm.addView(drawing, drawingParams);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setBackgroundColor(0xEE222222);
        tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(tools);
        add("رسم", () -> setDrawingMode(true));
        add("تفاعل", () -> setDrawingMode(false));
        add("أحمر", () -> { color = Color.RED; });
        add("أزرق", () -> { color = Color.BLUE; });
        add("أسود", () -> { color = Color.BLACK; });
        add("ممحاة", () -> { color = Color.TRANSPARENT; strokeWidth = 28f; });
        add("سُمك +", () -> { strokeWidth = Math.min(36f, strokeWidth + 3f); });
        add("سُمك -", () -> { strokeWidth = Math.max(2f, strokeWidth - 3f); });
        add("تراجع", () -> drawing.undo());
        add("مسح الكل", () -> drawing.clear());
        add("إخفاء", () -> drawing.setVisibility(View.GONE));
        add("إظهار", () -> drawing.setVisibility(View.VISIBLE));
        WindowManager.LayoutParams barParams = new WindowManager.LayoutParams(
            -1, -2, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        barParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wm.addView(scroll, barParams);
        setDrawingMode(true);
    }

    private void add(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11f);
        button.setPadding(6, 0, 6, 0);
        button.setOnClickListener(v -> action.run());
        tools.addView(button, new LinearLayout.LayoutParams(-2, 48));
    }

    private void setDrawingMode(boolean enabled) {
        drawingParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            (enabled ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        wm.updateViewLayout(drawing, drawingParams);
    }

    @Override public void onDestroy() {
        if (drawing != null) wm.removeView(drawing);
        super.onDestroy();
    }

    private class DrawView extends View {
        private final ArrayList<Path> paths = new ArrayList<>();
        private final ArrayList<Integer> colors = new ArrayList<>();
        private final ArrayList<Float> widths = new ArrayList<>();
        private Path current;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        DrawView() {
            super(DrawingOverlayService.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onDraw(Canvas canvas) {
            for (int i = 0; i < paths.size(); i++)
                drawPath(canvas, paths.get(i), colors.get(i), widths.get(i));
            if (current != null) drawPath(canvas, current, color, strokeWidth);
        }

        private void drawPath(Canvas canvas, Path path, int pathColor, float width) {
            paint.setColor(pathColor);
            paint.setStrokeWidth(width);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setXfermode(pathColor == Color.TRANSPARENT
                ? new PorterDuffXfermode(PorterDuff.Mode.CLEAR) : null);
            canvas.drawPath(path, paint);
            paint.setXfermode(null);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX(), y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                current = new Path();
                current.moveTo(x, y);
                invalidate();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && current != null) {
                current.lineTo(x, y);
                invalidate();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP && current != null) {
                current.lineTo(x, y);
                paths.add(current);
                colors.add(color);
                widths.add(strokeWidth);
                current = null;
                invalidate();
                return true;
            }
            return true;
        }

        void undo() {
            if (!paths.isEmpty()) {
                int last = paths.size() - 1;
                paths.remove(last); colors.remove(last); widths.remove(last);
                invalidate();
            }
        }

        void clear() {
            paths.clear(); colors.clear(); widths.clear(); current = null;
            invalidate();
        }
    }
}