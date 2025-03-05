import { Component, inject, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { DevService } from "../../../services/dev.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { MessageInterface } from "../../../domain/interfaces/message.interface";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { EditMessageDialogComponent } from "./edit-message-dialog/edit-message-dialog.component";

@Component({
  selector: "app-system",
  templateUrl: "./system.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatDialogModule,
  ],
})
export class SystemComponent {
  devService = inject(DevService);
  snackbarService = inject(SnackbarService);
  readonly dialog = inject(MatDialog);
  tokenForm: FormGroup;
  displayedColumns: string[] = ["id", "key", "value", "locale", "action"];
  messages: MatTableDataSource<MessageInterface> = new MatTableDataSource();

  @ViewChild(MatPaginator)
  paginator!: MatPaginator;
  @ViewChild(MatSort)
  sort!: MatSort;

  constructor() {
    this.tokenForm = new FormGroup({
      expiration: new FormControl<number>(0, [Validators.required]),
      attempts: new FormControl<number>(0, [Validators.required]),
    });
    this.loadInitialData();
  }

  loadInitialData() {
    this._getFailedAttempts();
    this._getMessages();
    this._getTokenExpirationTime();
  }

  updateTokenExpirationTime() {
    const time = this.tokenForm.value.expiration;

    this.devService
      .updateTokenExpirationTime(time)
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._getTokenExpirationTime();
      });
  }

  updateFailedAttempts() {
    const attempts = this.tokenForm.value.attempts;

    this.devService
      .updateFailedAttempts(attempts)
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._getFailedAttempts();
      });
  }

  edit(message: MessageInterface) {
    if (message != null) {
      const dialogRef = this.dialog.open(EditMessageDialogComponent, {
        data: { message: message },
      });
      dialogRef.afterClosed().subscribe((message) => {
        if (message)
          this.devService
            .updateMessage(message)
            .subscribe((response: ApiResponseInterface<MessageInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                3000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this._getMessages();
            });
      });
    } else
      this.snackbarService.openSnackbar(
        "Ocurrió un error el editar el elemento",
        3000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
  }

  private _getTokenExpirationTime() {
    this.devService
      .getTokenExpirationTime()
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            expiration: response.data,
          });
        }
      });
  }

  private _getFailedAttempts() {
    this.devService
      .getFailedAttempts()
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            attempts: response.data,
          });
        }
      });
  }

  private _getMessages() {
    this.devService
      .getMessages()
      .subscribe((response: ApiResponseInterface<MessageInterface[]>) => {
        this.messages = new MatTableDataSource(response.data);
        this.messages.paginator = this.paginator;
        this.messages.sort = this.sort;
      });
  }
}
