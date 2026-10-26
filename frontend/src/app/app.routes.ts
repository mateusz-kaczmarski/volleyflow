import { Component } from '@angular/core';
import { Routes } from '@angular/router';

@Component({ template: '' })
export class RouteOutletComponent {}

export const routes: Routes = [
  { path: '', component: RouteOutletComponent, pathMatch: 'full' },
  { path: 'activate', component: RouteOutletComponent },
  { path: 'clubs/:clubId/matches/:matchId/:tab', component: RouteOutletComponent },
  { path: 'clubs/:clubId/:page', component: RouteOutletComponent },
  { path: '**', redirectTo: '' }
];
