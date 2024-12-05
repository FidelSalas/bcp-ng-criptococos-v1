import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TradingRoutingModule } from './trading-routing.module';
import { TradingComponent } from './trading.component';


@NgModule({
  declarations: [
    TradingComponent
  ],
  imports: [
    CommonModule,
    TradingRoutingModule
  ]
})
export class TradingModule { }
