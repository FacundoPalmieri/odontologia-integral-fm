import { CommonModule } from "@angular/common";
import {
  Component,
  ElementRef,
  inject,
  Input,
  OnDestroy,
  signal,
  ViewChild,
} from "@angular/core";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { FileMetadataInterface } from "../../../domain/interfaces/patient.interface";
import { FileService } from "../../../services/file.service";
import { EntityTypeEnum } from "../../../utils/enums/entity-type.enum";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTableModule } from "@angular/material/table";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { Subject, takeUntil } from "rxjs";
import { MatTooltipModule } from "@angular/material/tooltip";

export interface AttachedFileEntityInterface {
  id: number;
  entity: EntityTypeEnum;
}

@Component({
  selector: "app-attached-file",
  templateUrl: "./attached-file.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatButtonModule,
    MatTableModule,
    MatTooltipModule,
  ],
})
export class AttachedFileComponent implements OnDestroy {
  private readonly fileService = inject(FileService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly _destroy$ = new Subject<void>();
  private _attachedFileEntity!: AttachedFileEntityInterface;

  filesMetadata = signal<FileMetadataInterface[]>([]);
  viewMode = signal<"list" | "grid">("grid");
  @ViewChild("attachedFileInput")
  attachedFileInput!: ElementRef<HTMLInputElement>;

  @Input({ required: true })
  set attachedFileEntity(value: AttachedFileEntityInterface) {
    this._attachedFileEntity = value;
    if (value) {
      this._getFiles(this._attachedFileEntity);
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  triggerAttachedFileInput(): void {
    this.attachedFileInput.nativeElement.click();
  }

  viewFile(fileId: number): void {
    switch (this._attachedFileEntity.entity) {
      case EntityTypeEnum.PATIENT:
        this.fileService
          .downloadPatientFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            window.open(url, "_blank");
          });
        break;
      case EntityTypeEnum.USER:
        this.fileService
          .downloadUserFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            window.open(url, "_blank");
          });
        break;
    }
  }

  deleteFile(fileId: number): void {
    switch (this._attachedFileEntity.entity) {
      case EntityTypeEnum.PATIENT:
        this.fileService
          .deletePatientFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((response: ApiResponseInterface<string>) => {
            this.snackbarService.openSnackbar(
              response.message,
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this._getFiles(this._attachedFileEntity);
          });
        break;
      case EntityTypeEnum.USER:
        this.fileService
          .deleteUserFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((response: ApiResponseInterface<string>) => {
            this.snackbarService.openSnackbar(
              response.message,
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this._getFiles(this._attachedFileEntity);
          });
        break;
    }
  }

  downloadFile(fileId: number, fileName: string): void {
    switch (this._attachedFileEntity.entity) {
      case EntityTypeEnum.PATIENT:
        this.fileService
          .downloadPatientFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            a.click();
            window.URL.revokeObjectURL(url);
          });
        break;
      case EntityTypeEnum.USER:
        this.fileService
          .downloadUserFile(fileId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            a.click();
            window.URL.revokeObjectURL(url);
          });
        break;
    }
  }

  onStudyFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const allowedTypes = [
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ];
    if (!allowedTypes.includes(file.type)) {
      this.snackbarService.openSnackbar(
        "El archivo debe ser PDF o Word (doc, docx)",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
      return;
    }

    switch (this._attachedFileEntity.entity) {
      case EntityTypeEnum.PATIENT:
        this.fileService
          .uploadPatientFile(this._attachedFileEntity.id, file)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: () => {
              this.fileService
                .getPatientFilesMetadata(this._attachedFileEntity.id)
                .subscribe(
                  (response: ApiResponseInterface<FileMetadataInterface[]>) => {
                    this.filesMetadata.set(response.data);
                  }
                );
              this.snackbarService.openSnackbar(
                "Archivo subido.",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
            },
            error: () => {},
          });
        break;
      case EntityTypeEnum.USER:
        this.fileService
          .uploadUserFile(this._attachedFileEntity.id, file)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: () => {
              this.fileService
                .getUserFilesMetadata(this._attachedFileEntity.id)
                .subscribe(
                  (response: ApiResponseInterface<FileMetadataInterface[]>) => {
                    this.filesMetadata.set(response.data);
                  }
                );
              this.snackbarService.openSnackbar(
                "Archivo subido.",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
            },
            error: () => {},
          });
        break;
    }
  }

  private _getFiles(attachedFileEntity: AttachedFileEntityInterface) {
    switch (attachedFileEntity.entity) {
      case EntityTypeEnum.USER:
        this.fileService
          .getUserFilesMetadata(attachedFileEntity.id)
          .subscribe(
            (response: ApiResponseInterface<FileMetadataInterface[]>) => {
              this.filesMetadata.set(response.data);
            }
          );
        break;
      case EntityTypeEnum.PATIENT:
        this.fileService
          .getPatientFilesMetadata(attachedFileEntity.id)
          .subscribe(
            (response: ApiResponseInterface<FileMetadataInterface[]>) => {
              this.filesMetadata.set(response.data);
            }
          );
    }
  }

  getFileExtension(fileName: string): string {
    return fileName.split(".").pop() || "";
  }

  toggleViewMode(): void {
    this.viewMode.set(this.viewMode() === "list" ? "grid" : "list");
  }
}
