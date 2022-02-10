def view(window=5, scale=100, refresh=0.2, figure="15x6", backend='TkAgg'):
    from . import viewer_v1
    viewer_v1.view(window, scale, refresh, figure, backend)