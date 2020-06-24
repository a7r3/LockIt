package nooblife.lockit.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class MarginDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable divider;
    private Context context;

    public MarginDividerItemDecoration(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = styledAttributes.getDrawable(0);
        this.context = context;
        styledAttributes.recycle();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        // Setting the margin for this Divider, which would be drawn between Views belonging to user.xml
        // The Divider should appear only for the Textual Details

        // Left Margin = Width of the Profile Picture = 8dp + 64dp (Actual width) + 8dp = 80dp
        int left = (int) Utils.convertDpToPixel(80.0f, context);
        // Right Margin = 16dp
        int rightSub = (int) Utils.convertDpToPixel(16.0f, context);

        c.save();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            // Get the view at i'th position
            View child = parent.getChildAt(i);
            int bottom = child.getBottom();
            int top = bottom - divider.getIntrinsicHeight();
            int right = child.getWidth() - rightSub;
            // Setting the bounds for this divider
            // The Divider would restrict itself under these constraints
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
        c.restore();
    }
}