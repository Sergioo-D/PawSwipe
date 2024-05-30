from django.urls import path
from .views import *

urlpatterns = [
    path('password_reset/',restablecer_pass, name='password_reset'),
    path('password_reset_done/', password_reset_done, name='password_reset_done'),
    path('password_reset_mail/', password_reset_mail, name='pass_reset_mail'),
    path('password_reset_confirm/<uidb64>/<token>/', password_reset_confirm, name='pass_reset_confirm'),
    path('password_reset_complete/', password_reset_complete, name='pass_reset_complete'),
]