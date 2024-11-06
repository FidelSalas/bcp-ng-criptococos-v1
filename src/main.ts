/* eslint-disable no-console */
/* eslint-disable id-blacklist */
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { defineCustomElements } from '@bcp/stl-ui-components/loader';
import { AppModule } from './app/app.module';

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

// eslint-disable-next-line @typescript-eslint/no-floating-promises
defineCustomElements(window);
