import { Component, OnInit, Input, SimpleChanges, OnChanges } from '@angular/core';
import { get } from "../../utils/request"


@Component({
  selector: 'custom-activity-table',
  templateUrl: './activity-table.component.html',
  styleUrls: ['./activity-table.component.css']
})
export class ActivityTableComponent implements OnInit {
  incidents: string;

  @Input() processDefinitionId: string;

  @Input()
  set activityId(activityId: string) {
    this._activityId = activityId;
    // make a rest call
    this.getIncidents();
  }
  get activityId(): string { return this._activityId; }
  _activityId: string;


  private getIncidents() {
    const args = { maxResults: 500, processDefinitionId: this.processDefinitionId };
    if (this.activityId) {
      args['activityId'] = this.activityId;
    }
    get("%API%/engine/%ENGINE%/incident", args)
      .then(async res => {
        const json = await res.json();
        this.incidents = json;
        console.log(json);
      })
  }

  constructor() { }

  ngOnInit() {
    this.getIncidents();
  }

}
