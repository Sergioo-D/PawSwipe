# Generated by Django 5.0 on 2024-05-06 17:40

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('bbdd', '0009_remove_perfil_numseguidores_and_more'),
    ]

    operations = [
        migrations.AddField(
            model_name='perfil',
            name='totalPublicaciones',
            field=models.IntegerField(default=0),
        ),
    ]
