# Generated by Django 5.0 on 2024-04-26 15:20

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('bbdd', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='mascota',
            name='nombre',
            field=models.CharField(max_length=50, unique=True),
        ),
    ]
