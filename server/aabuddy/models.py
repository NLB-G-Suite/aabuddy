''' module for aabuddy models '''
from django.contrib.gis.db import models
from django.db import models as classic_models
import datetime
from django.contrib.gis.db.models.manager import GeoManager
from django.contrib.auth.models import User


class Meeting(models.Model):
    ''' meeting models class '''

    SUBMITTED = "submitted"
    APPROVED = "approved"
    REMOVED = "removed"
    INTERNAL_TYPE_CHOICES = [(SUBMITTED, "Submitted"),
                             (APPROVED, "Approved"),
                             (REMOVED, "Removed")]
    
    DAY_OF_WEEK_CHOICES = [(1, "Sunday"),
                           (2, "Monday"),
                           (3, "Tuesday"),
                           (4, "Wednesday"),
                           (5, "Thursday"),
                           (6, "Friday"),
                           (7, "Saturday")]

    objects = GeoManager()
    day_of_week = models.IntegerField(null=False, blank=False, default=1, choices=DAY_OF_WEEK_CHOICES)
    start_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    end_time = models.TimeField(null=False, blank=False, default=datetime.time(12, 30))
    name = models.CharField(max_length=100)
    description = models.CharField(max_length=255, null=True, blank=True)
    address = models.CharField(max_length=300)
    internal_type = models.CharField(max_length=10, default=SUBMITTED, choices=INTERNAL_TYPE_CHOICES)
    creator = models.ForeignKey(User, related_name='meetings', null=True, on_delete=models.CASCADE)
    created_date = models.DateTimeField(editable=False,null=False, blank=False, default=datetime.datetime(1982,12,22))
    geo_location = models.PointField()
    
    def __str__(self):
        return ("Meeting id: %s, Creator: %s" % (self.pk, self.creator.username))

    def save(self, **kwargs):
        if not self.id:
            self.created_date = datetime.datetime.now() # Edit created timestamp only if it's new entry
        super(Meeting, self).save()


class MeetingNotThere(classic_models.Model):
    meeting = classic_models.ForeignKey(Meeting, related_name='not_theres', null=False, blank=False, on_delete=classic_models.CASCADE)
    user = models.ForeignKey(User, related_name='not_theres', null=True, blank=True, on_delete=classic_models.SET_NULL)
    request_host = classic_models.CharField(max_length=200, null=True, blank=True)
    user_agent = classic_models.CharField(max_length=400, null=True, blank=True)
    unique_phone_id = classic_models.CharField(max_length=400, null=True, blank=True)
    created_date = models.DateTimeField(auto_now_add=True)
    def __str__(self):
        return ("Not There id: %s, host: %s" % (self.pk, self.request_host))


class UserConfirmation(models.Model):
    user = models.ForeignKey(User, related_name='confirmations', null=False, blank=False, on_delete=models.CASCADE)
    created_date = models.DateTimeField(null=False, blank=False, default=datetime.datetime.now())
    expiration_date = models.DateTimeField(null=False, blank=False)
    confirmation_key = models.CharField(max_length=64, null=False, blank=False)
    