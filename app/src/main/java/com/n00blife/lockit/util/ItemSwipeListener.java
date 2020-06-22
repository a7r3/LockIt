package com.n00blife.lockit.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.n00blife.lockit.R;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class ItemSwipeListener extends ItemTouchHelper.SimpleCallback {

    private ColorDrawable colorDrawable = new ColorDrawable();
    private Drawable deleteDrawable;
    private Drawable activateDrawable;
    private Paint clearPaint = new Paint();

    public ItemSwipeListener(Context context) {
        super(0, LEFT | RIGHT);
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        activateDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return 0.5f;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;

        int itemViewHeight = itemView.getBottom() - itemView.getTop();
        boolean isCanceled = dX == 0f && isCurrentlyActive;

        if (isCanceled) {
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            c.drawRect(itemView.getRight() - dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), clearPaint);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        Drawable someDrawable;

        someDrawable = activateDrawable;
        colorDrawable.setColor(Color.parseColor("#4CAF50"));

        int deleteIconTop = itemView.getTop() + (itemViewHeight - someDrawable.getIntrinsicHeight()) / 2;
        int deleteIconMargin = (itemViewHeight - someDrawable.getIntrinsicHeight()) / 2;
        int deleteIconBottom = deleteIconTop + someDrawable.getIntrinsicHeight();

        int deleteIconLeft = itemView.getLeft() + deleteIconMargin;
        int deleteIconRight = itemView.getLeft() + deleteIconMargin + someDrawable.getIntrinsicWidth();

        if (dX < 0) {
            someDrawable = deleteDrawable;
            colorDrawable.setColor(Color.parseColor("#F44336"));
            deleteIconLeft = itemView.getRight() - deleteIconMargin - someDrawable.getIntrinsicWidth();
            deleteIconRight = itemView.getRight() - deleteIconMargin;
        }

        colorDrawable.setBounds(
                (dX < 0) ? itemView.getRight() + (int) dX : itemView.getLeft(),
                itemView.getTop(),
                (dX < 0) ? itemView.getRight() : itemView.getLeft() + (int) dX,
                itemView.getBottom()
        );

        colorDrawable.draw(c);

        someDrawable.setBounds(deleteIconLeft,
                deleteIconTop,
                deleteIconRight,
                deleteIconBottom
        );

        someDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
