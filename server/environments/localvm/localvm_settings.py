DATABASES = {
    'default': {
        'ENGINE': 'django.contrib.gis.db.backends.postgis', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': 'aabuddy', # Or path to database file if using sqlite3.
        'USER': 'postgres', # Not used with sqlite3.
        'PASSWORD': '', # Not used with sqlite3.
        'HOST': 'localhost', # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '5432', # Set to empty string for default. Not used with sqlite3.
        }
}

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format': '%(levelname)s %(asctime)s %(module)s %(process)d %(thread)d %(message)s'
        },
        'simple': {
            'format': '%(levelname)s %(message)s'
        },
    },
    'handlers': {
        'default': {
            'level':'DEBUG',
            'class':'logging.handlers.RotatingFileHandler',
            'filename': '/var/www/aabuddy/logs/aabuddy.log',
            'maxBytes': 1024*1024*5, # 5 MB
            'backupCount': 5,
            'formatter':'verbose',
        }       
    },
    'loggers': {
        'aabuddy': {
            'handlers': ['default'],
            'level': 'DEBUG'
        },
        'django.request': {
            'handlers': ['default'],
            'level': 'DEBUG',
            'propagate': True,
        },
    }
}

EMAIL_SUBJECT_PREFIX = '[AA Buddy] '
SERVER_EMAIL = 'codecleric@gmail.com'
EMAIL_HOST = 'smtp.gmail.com'
EMAIL_HOST_PASSWORD = '3rsheam5'
EMAIL_HOST_USER = 'codecleric@gmail.com'
EMAIL_PORT = 587
EMAIL_USE_TLS = True

LOGTAIL_FILES = {
                 'django': '/var/www/aabuddy/logs/aabuddy.log',
                 'apache_access': '/var/www/aabuddy/logs/access.log',
                 'apache_error': '/var/www/aabuddy/logs/error.log'
}

POSTGIS_VERSION=(2,0,1)