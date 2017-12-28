package com.my.core.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableHashSet<T> extends HashSet<T> {

    private final Object mutex = new Object();

    private Handler mHandler;
    private List<OnSetChangedListener<T>> mCallbacks;

    @SuppressWarnings("unused")
    public ObservableHashSet() {
        super();
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(Collection<? extends T> c) {
        super(c);
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(int initialCapacity) {
        super(initialCapacity);
        ensureHandler();
    }

    @Override
    public int size() {
        synchronized (mutex) {
            return super.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (mutex) {
            return super.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (mutex) {
            return super.contains(o);
        }
    }

    @Override
    public boolean add(T t) {
        synchronized (mutex) {
            final boolean changed = super.add(t);

            if (changed) {
                dispatchOnSetChangedCallbacks();
            }

            return changed;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (mutex) {
            final boolean changed = super.remove(o);

            if (changed) {
                dispatchOnSetChangedCallbacks();
            }

            return changed;
        }
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            super.clear();
            dispatchOnSetChangedCallbacks();
        }
    }

    @SuppressWarnings("unused")
    public void addOnSetChangedListener(OnSetChangedListener<T> listener) {
        synchronized (mutex) {
            // Ensure the list.
            if (mCallbacks == null) {
                mCallbacks = new CopyOnWriteArrayList<>();
            }

            mCallbacks.add(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeOnSetChangedListener(OnSetChangedListener<T> listener) {
        synchronized (mutex) {
            if (mCallbacks == null) return;

            mCallbacks.remove(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeAllOnSetChangedListener() {
        synchronized (mutex) {
            if (mCallbacks == null) return;

            mCallbacks.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void ensureHandler() {
        mHandler = new Handler(Looper.myLooper());
    }

    private void dispatchOnSetChangedCallbacks() {
        if (mHandler == null) return;

        final ObservableHashSet<T> thiz = this;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallbacks == null) return;

                for (OnSetChangedListener<T> callback : mCallbacks) {
                    callback.onSetChanged(thiz);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface Provider<E> {
        ObservableHashSet<E> getObservableSet();
    }

    public interface OnSetChangedListener<T> {
        void onSetChanged(final ObservableHashSet<T> set);
    }
}
