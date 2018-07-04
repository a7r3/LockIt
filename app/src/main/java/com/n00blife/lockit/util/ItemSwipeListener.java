package com.n00blife.lockit.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.n00blife.lockit.R;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;

public class ItemSwipeListener extends ItemTouchHelper.SimpleCallback {

    private ColorDrawable colorDrawable = new ColorDrawable();
    private Context context;
    private Drawable deleteDrawable;
    private Paint clearPaint = new Paint();

    public ItemSwipeListener(Context context) {
        super(0, LEFT);
        this.context = context;
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

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

        colorDrawable.setColor(Color.parseColor("#F44336"));
        colorDrawable.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );

        colorDrawable.draw(c);

        int deleteIconTop = itemView.getTop() + (itemViewHeight - deleteDrawable.getIntrinsicHeight()) / 2;
        int deleteIconMargin = (itemViewHeight - deleteDrawable.getIntrinsicHeight()) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteDrawable.getIntrinsicWidth();
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconBottom = deleteIconTop + deleteDrawable.getIntrinsicHeight();

        deleteDrawable.setBounds(deleteIconLeft,
                deleteIconTop,
                deleteIconRight,
                deleteIconBottom
        );

        deleteDrawable.draw(c);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
