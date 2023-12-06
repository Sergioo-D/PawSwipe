# Generated by Django 4.2.5 on 2023-12-04 14:59

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        ('auth', '0012_alter_user_first_name_max_length'),
    ]

    operations = [
        migrations.CreateModel(
            name='Usuario',
            fields=[
                ('password', models.CharField(max_length=128, verbose_name='password')),
                ('is_superuser', models.BooleanField(default=False, help_text='Designates that this user has all permissions without explicitly assigning them.', verbose_name='superuser status')),
                ('identificador', models.AutoField(primary_key=True, serialize=False)),
                ('nombreUsuario', models.CharField(max_length=40, unique=True)),
                ('mail', models.EmailField(max_length=254, unique=True)),
                ('nombreReal', models.CharField(max_length=50)),
                ('fecha', models.DateField(auto_now_add=True)),
                ('last_login', models.DateTimeField(auto_now=True)),
                ('is_staff', models.BooleanField(default=False)),
                ('groups', models.ManyToManyField(blank=True, help_text='The groups this user belongs to. A user will get all permissions granted to each of their groups.', related_name='usuario_groups', related_query_name='usuario', to='auth.group', verbose_name='groups')),
                ('user_permissions', models.ManyToManyField(blank=True, help_text='Specific permissions for this user.', related_name='usuario_user_permissions', related_query_name='usuario', to='auth.permission', verbose_name='user permissions')),
            ],
            options={
                'db_table': 'usuario',
            },
        ),
    ]