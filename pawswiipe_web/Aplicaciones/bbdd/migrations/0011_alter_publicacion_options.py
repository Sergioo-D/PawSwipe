# Generated by Django 5.0 on 2024-05-08 00:54

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('bbdd', '0010_perfil_totalpublicaciones'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='publicacion',
            options={'ordering': ['fechaPublicacion']},
        ),
    ]
