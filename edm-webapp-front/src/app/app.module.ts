import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { FlexLayoutModule } from "@angular/flex-layout";

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatToolbarModule, MatIconModule, MatInputModule, MatButtonModule, MatCheckboxModule, MatCardModule } from '@angular/material';

import { PdsSearchService } from './services/pds-search.service';
import { PdsDocumentService } from './services/pds-document.service';
import { PdsCategoryService } from './services/pds-category.service';

import { PdsPageSearchComponent } from './pds-page-search/pds-page-search.component';
import { PdsToolbarComponent } from './pds-toolbar/pds-toolbar.component';
import { PdsCloudTagsComponent } from './pds-cloud-tags/pds-cloud-tags.component';
import { PdsCheckableListWithCountComponent } from './pds-checkable-list-with-count/pds-checkable-list-with-count.component';
import { PdsCheckableCategoriesListComponent } from './pds-checkable-categories-list/pds-checkable-categories-list.component';

@NgModule({
  declarations: [
    PdsPageSearchComponent,
    PdsToolbarComponent,
    PdsCloudTagsComponent,
    PdsCheckableCategoriesListComponent,
    PdsCheckableListWithCountComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    FlexLayoutModule,
    BrowserAnimationsModule,
    MatAutocompleteModule,
    MatToolbarModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatCardModule
  ],
  providers: [
    PdsSearchService,
    PdsDocumentService,
    PdsCategoryService
  ],
  bootstrap: [PdsToolbarComponent, PdsPageSearchComponent]
})
export class PdsModule { }
