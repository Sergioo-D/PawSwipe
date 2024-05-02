# Generated by Django 5.0 on 2024-05-01 03:59

import django.db.models.deletion
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('bbdd', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Publicacion',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('descripcion', models.TextField(blank=True)),
                ('fechaPublicacion', models.DateTimeField(auto_now_add=True)),
                ('perfil', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='publicaciones', to='bbdd.perfil')),
            ],
        ),
        migrations.CreateModel(
            name='Imagen',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('urlImagen', models.URLField()),
                ('descripcionImagen', models.TextField(blank=True)),
                ('publicacion', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='imagenes', to='bbdd.publicacion')),
            ],
        ),
    ]
